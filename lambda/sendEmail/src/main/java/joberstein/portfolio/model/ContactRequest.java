package joberstein.portfolio.model;

import com.amazonaws.services.simpleemail.model.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.StandardCharsets;

@Data
@Builder
@JsonDeserialize(builder = ContactRequest.ContactRequestBuilder.class)
public class ContactRequest {
    private static final String GOOGLE_CAPTCHA_KEY = "GOOGLE_CAPTCHA_KEY";

    private String from;
    private String replyToAddress;
    private String subject;
    private String body;
    private String captcha;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ContactRequestBuilder {}

    public VerifyCaptchaRequest toCaptchaRequest(String ipAddress) {
        return VerifyCaptchaRequest.builder()
            .response(this.captcha)
            .secret(System.getenv(GOOGLE_CAPTCHA_KEY))
            .remoteIp(ipAddress)
            .build();
    }

    public SendEmailRequest toSendEmailRequest(String sourceEmail, String destinationEmail, String messageTemplate) {
        var subject = new Content()
            .withCharset(StandardCharsets.UTF_8.name())
            .withData(StringEscapeUtils.escapeEcmaScript(this.subject));

        var emailHtml = new Content()
            .withCharset(StandardCharsets.UTF_8.name())
            .withData(String.format(
                messageTemplate,
                StringEscapeUtils.escapeEcmaScript(this.replyToAddress),
                StringEscapeUtils.escapeEcmaScript(this.from.isEmpty() ? this.replyToAddress : this.from),
                StringEscapeUtils.escapeEcmaScript(this.body)));

        var body = new Body()
            .withHtml(emailHtml);

        var message = new Message()
            .withSubject(subject)
            .withBody(body);
            
        var destination = new Destination()
            .withToAddresses(destinationEmail);

        return new SendEmailRequest()
            .withSource(sourceEmail)
            .withMessage(message)
            .withDestination(destination);
    }
}
