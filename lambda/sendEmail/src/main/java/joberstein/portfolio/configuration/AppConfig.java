package joberstein.portfolio.configuration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import joberstein.portfolio.service.CaptchaVerificationService;
import joberstein.portfolio.http.UnirestClient;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConfig {
    private static final Regions SES_REGION = Regions.US_EAST_1;
    
    private static AppConfig instance;
    private AmazonSimpleEmailService simpleEmailService;
    private CaptchaVerificationService captchaVerificationService;

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        
        return instance;
    }
    
    public AmazonSimpleEmailService getSimpleEmailService() {
        if (simpleEmailService == null) {
            simpleEmailService = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(SES_REGION).build();
        }
        
        return simpleEmailService;
    }

    public CaptchaVerificationService getCaptchaVerificationService(LambdaLogger logger) {
        if (captchaVerificationService == null || !captchaVerificationService.getHttpClient().isRunning()) {
            var httpClient = UnirestClient.newInstance(logger);
            captchaVerificationService = new CaptchaVerificationService(httpClient);
        }

        return captchaVerificationService;
    }
}