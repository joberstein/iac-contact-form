package joberstein.portfolio.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

/**
 * Modeled after the Google Captcha Verification API request format: 
 * https://developers.google.com/recaptcha/docs/verify
 */
@Data
@Builder
public class VerifyCaptchaRequest {
    private String secret;
    private String response;

    @JsonProperty("remoteip")
    private String remoteIp;
}
