package joberstein.portfolio;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import joberstein.portfolio.configuration.AppConfig;
import joberstein.portfolio.model.ContactRequest;
import joberstein.portfolio.model.ContactResponse;
import joberstein.portfolio.model.VerifyCaptchaRequest;
import joberstein.portfolio.model.VerifyCaptchaResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext;
import static com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class SendEmailHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final static String SOURCE_EMAIL = "portfolio@jesseoberstein.com";
    private final static String DESTINATION_EMAIL = "joberstein12@gmail.com";
    private final static String MESSAGE_HTML_TEMPLATE = 
        "<html>" +
            "<body>" +
                "<h3>" +
                    "<a href='mailto:%s'>%s</a> sent you a message using the contact form on your portfolio website." +
                "</h3>" +
                "<p>Here's the message they sent you:</p>" +
                "<p>%s</p>" +
            "</body>" +
        "</html>";

    private final AppConfig config;
    private final ObjectMapper mapper;

    public SendEmailHandler() {
        config = AppConfig.getInstance();
        mapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        var response = new APIGatewayProxyResponseEvent();
        ContactRequest contactRequest = parseRequestBody(request, logger);

        if (contactRequest == null) {
            return response
                .withStatusCode(400)
                .withBody("Could not parse request body.");
        }

        String ipAddress = getIpAddress(request);
        var captchaRequest = contactRequest.toCaptchaRequest(ipAddress);
        var captchaResponse = performCaptchaVerification(captchaRequest, logger);

        if (!captchaResponse.isSuccess()) {
            return response
                .withStatusCode(403)
                .withBody("Captcha validation failed.");
        }

        SendEmailResult emailResult = sendEmail(contactRequest, logger);

        if (emailResult == null) {
            return response
                .withStatusCode(500)
                .withBody("Could not send the email.");
        }

        var responseBody = buildResponseBody(emailResult, logger);

        if (responseBody == null) {
            return response
                .withStatusCode(500)
                .withBody("Could not generate a valid response body.");
        }

        return response
            .withStatusCode(200)
            .withBody(responseBody);
    }

    private ContactRequest parseRequestBody(APIGatewayProxyRequestEvent request, LambdaLogger logger) {
        try {
            return mapper.readValue(request.getBody(), ContactRequest.class);
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return null;
        }
    }

    private String getIpAddress(APIGatewayProxyRequestEvent request) {
        return Optional.ofNullable(request)
            .map(APIGatewayProxyRequestEvent::getRequestContext)
            .map(ProxyRequestContext::getIdentity)
            .map(RequestIdentity::getSourceIp)
            .orElse("");
    }

    private VerifyCaptchaResponse performCaptchaVerification(VerifyCaptchaRequest captchaRequest, LambdaLogger logger) {
        var captchaVerificationService = config.getCaptchaVerificationService(logger);
        var captchaResponse = captchaVerificationService.verify(captchaRequest);
        captchaVerificationService.getHttpClient().close();
        return captchaResponse;
    }

    private SendEmailResult sendEmail(ContactRequest contactRequest, LambdaLogger logger) {
        try {
            AmazonSimpleEmailService ses = config.getSimpleEmailService();
            var emailRequest = contactRequest.toSendEmailRequest(SOURCE_EMAIL, DESTINATION_EMAIL, MESSAGE_HTML_TEMPLATE);
            return ses.sendEmail(emailRequest);
        } catch (Exception e) {
            logger.log(e.getMessage());
            return null;
        }
    }

    private String buildResponseBody(SendEmailResult emailResult, LambdaLogger logger) {
        try {
            var contactResponse = new ContactResponse(emailResult.getMessageId());
            return mapper.writeValueAsString(contactResponse);
        } catch (JsonProcessingException e) {
            logger.log(e.getMessage());
            return null;
        }
    }
}