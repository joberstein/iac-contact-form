package joberstein.portfolio;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import joberstein.portfolio.configuration.AppConfig;
import joberstein.portfolio.http.HttpClient;
import joberstein.portfolio.model.ContactRequest;
import joberstein.portfolio.model.ContactResponse;
import joberstein.portfolio.model.VerifyCaptchaRequest;
import joberstein.portfolio.model.VerifyCaptchaResponse;
import joberstein.portfolio.service.CaptchaVerificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import static com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailHandlerTest {

    @Mock
    private Context context;

    @Mock
    private LambdaLogger logger;

    @Mock
    private AmazonSimpleEmailService ses;

    @Mock
    private CaptchaVerificationService captchaVerificationService;

    @Mock
    private HttpClient httpClient;

    @Mock
    private AppConfig config;

    private final ObjectMapper mapper = new ObjectMapper();

    private SendEmailHandler application;

    private final String CAPTCHA_TOKEN = "captcha_token";
    private final String MESSAGE_ID = "sent-message-id";
    private final ContactRequest CONTACT_REQUEST = ContactRequest.builder()
        .from("From")
        .subject("Subject")
        .replyToAddress("test@test.com")
        .body("Body")
        .captcha(CAPTCHA_TOKEN)
        .build();

    @Before
    public void setup() {
        VerifyCaptchaResponse captchaResponse = VerifyCaptchaResponse.builder().success(true).build();

        when(config.getCaptchaVerificationService(logger)).thenReturn(captchaVerificationService);
        when(captchaVerificationService.verify(any(VerifyCaptchaRequest.class))).thenReturn(captchaResponse);
        when(captchaVerificationService.getHttpClient()).thenReturn(httpClient);
        doNothing().when(httpClient).close();

        when(config.getSimpleEmailService()).thenReturn(ses);
        when(ses.sendEmail(any(SendEmailRequest.class))).thenReturn(new SendEmailResult().withMessageId(MESSAGE_ID));

        when(context.getLogger()).thenReturn(logger);

        spy(mapper);

        application = new SendEmailHandler(config, mapper);
    }

    @Test
    public void testHandleRequest_invalidRequest() throws JsonProcessingException {
        var request = new APIGatewayProxyRequestEvent().withBody("invalid-body");
        var response = application.handleRequest(request, context);

        assertEquals(400, response.getStatusCode().intValue());
        verify(logger).log(anyString());
        verifyZeroInteractions(captchaVerificationService);
        verifyZeroInteractions(ses);
    }

    @Test
    public void testHandleRequest_invalidCaptcha() throws JsonProcessingException {
        VerifyCaptchaResponse captchaResponse = VerifyCaptchaResponse.builder().success(false).build();
        when(captchaVerificationService.verify(any(VerifyCaptchaRequest.class))).thenReturn(captchaResponse);

        var request = buildApiGatewayRequest(CONTACT_REQUEST);
        var response = application.handleRequest(request, context);

        assertEquals(403, response.getStatusCode().intValue());
        verifyZeroInteractions(logger);
        verify(captchaVerificationService).verify(any(VerifyCaptchaRequest.class));
        verifyZeroInteractions(ses);
    }

    @Test
    public void testHandleRequest_validCaptcha() throws JsonProcessingException {
        var request = buildApiGatewayRequest(CONTACT_REQUEST);
        var response = application.handleRequest(request, context);
        var contactResponse = mapper.readValue(response.getBody(), ContactResponse.class);

        assertEquals(200, response.getStatusCode().intValue());
        assertEquals(MESSAGE_ID, contactResponse.getResultId());
        verifyZeroInteractions(logger);
        verify(captchaVerificationService).verify(any(VerifyCaptchaRequest.class));
        verify(ses).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    public void testHandleRequest_sendingEmailFails() throws JsonProcessingException {
        when(ses.sendEmail(any(SendEmailRequest.class))).thenThrow(new RuntimeException("Some problem."));

        var request = buildApiGatewayRequest(CONTACT_REQUEST);
        var response = application.handleRequest(request, context);

        assertEquals(500, response.getStatusCode().intValue());
        verify(logger).log(anyString());
        verify(captchaVerificationService).verify(any(VerifyCaptchaRequest.class));
        verify(ses).sendEmail(any(SendEmailRequest.class));
    }

    private APIGatewayProxyRequestEvent buildApiGatewayRequest(ContactRequest contactRequest) throws JsonProcessingException {
        var requestIdentity = new RequestIdentity()
            .withSourceIp("test-ip");

        var requestContext = new ProxyRequestContext()
            .withIdentity(requestIdentity);

        return new APIGatewayProxyRequestEvent()
            .withRequestContext(requestContext)
            .withBody(mapper.writeValueAsString(contactRequest));
    }
}
