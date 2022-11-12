package joberstein.portfolio.service;

import joberstein.portfolio.http.HttpClient;
import joberstein.portfolio.model.VerifyCaptchaRequest;
import joberstein.portfolio.model.VerifyCaptchaResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@AllArgsConstructor
public class CaptchaVerificationService {
    public final static String CAPTCHA_VERIFICATION_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

    private HttpClient httpClient;

    /**
     * Request the captcha verification API with the given captcha request.
     */
    public VerifyCaptchaResponse verify(VerifyCaptchaRequest request) {
        var qsTypeRef = new TypeReference<Map<String,Object>>(){};
        var queryParams = MAPPER.convertValue(request, qsTypeRef);
        var response = httpClient.postForObject(
                CAPTCHA_VERIFICATION_URL, 
                queryParams, 
                VerifyCaptchaResponse.class);
            
        return response == null ? VerifyCaptchaResponse.builder().build() : response;
    }
}
