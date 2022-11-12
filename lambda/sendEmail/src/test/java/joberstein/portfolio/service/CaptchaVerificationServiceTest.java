package joberstein.portfolio.service;

import joberstein.portfolio.http.HttpClient;
import joberstein.portfolio.model.VerifyCaptchaRequest;
import joberstein.portfolio.model.VerifyCaptchaResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CaptchaVerificationServiceTest {

    @InjectMocks
    private CaptchaVerificationService captchaVerificationService;

    @Mock
    private HttpClient httpClient;

    @Captor
    private ArgumentCaptor<Map<String,Object>> qsCaptor;

    @Test
    public void testVerifyCaptcha_succeeds() {
        var response = captchaResponseBuilder().build();
        mockVerificationRequest(response);

        var request = captchaRequestBuilder().build();
        assertTrue(captchaVerificationService.verify(request).isSuccess());

        verifyMockedVerificationRequest();
        
        var queryParams = qsCaptor.getValue();
        assertEquals("aaa", queryParams.get("response"));
        assertEquals("bbb", queryParams.get("secret"));
        assertEquals("ccc", queryParams.get("remoteip"));
    }

    @Test
    public void testVerifyCaptcha_succeeds_remoteIpNull() {
        var response = captchaResponseBuilder().build();
        mockVerificationRequest(response);

        var request = captchaRequestBuilder().remoteIp(null).build();
        assertTrue(captchaVerificationService.verify(request).isSuccess());

        verifyMockedVerificationRequest();

        var queryParams = qsCaptor.getValue();
        assertEquals("aaa", queryParams.get("response"));
        assertEquals("bbb", queryParams.get("secret"));
        assertFalse(queryParams.containsKey("remoteip"));
    }

    @Test
    public void testVerifyCaptcha_fails() {
        var response = captchaResponseBuilder().success(false).build();
        mockVerificationRequest(response);

        var request = captchaRequestBuilder().build();
        assertFalse(captchaVerificationService.verify(request).isSuccess());
    }


    private void mockVerificationRequest(VerifyCaptchaResponse response) {
        when(httpClient.postForObject(anyString(), anyMap(), eq(VerifyCaptchaResponse.class)))
            .thenReturn(response);
    }
    
    private void verifyMockedVerificationRequest() {
        verify(httpClient).postForObject(
            eq(CaptchaVerificationService.CAPTCHA_VERIFICATION_URL),
            qsCaptor.capture(), 
            eq(VerifyCaptchaResponse.class));
    }

    private VerifyCaptchaResponse.VerifyCaptchaResponseBuilder captchaResponseBuilder() {
        return VerifyCaptchaResponse.builder()
            .success(true);
    }

    private VerifyCaptchaRequest.VerifyCaptchaRequestBuilder captchaRequestBuilder() {
        return VerifyCaptchaRequest.builder()
            .response("aaa")
            .secret("bbb")
            .remoteIp("ccc");
    }
}
