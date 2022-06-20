package joberstein.portfolio;

import com.amazonaws.regions.AwsRegionProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import joberstein.portfolio.apiGateway.MessagesApi;
import joberstein.portfolio.iam.SendEmailRole;
import joberstein.portfolio.lambda.SendEmailLambda;
import joberstein.portfolio.s3.SendEmailBucket;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;

public class TemplateBuilder {

    public static void main(String[] args) {
        App app = new App();

        var getCallerIdentityRequest = new GetCallerIdentityRequest();
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard().build();
        String accountId = sts.getCallerIdentity(getCallerIdentityRequest).getAccount();

        AwsRegionProviderChain regionProvider = new DefaultAwsRegionProviderChain();

        Environment env = Environment.builder()
            .account(accountId)
            .region(regionProvider.getRegion())
            .build();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();

        var sendEmailBucket = new SendEmailBucket(app, env);
        var sendEmailRole = new SendEmailRole(app, env);
        var sendEmailLambda = new SendEmailLambda(app, env, s3Client, sendEmailRole, sendEmailBucket);
        new MessagesApi(app, env, sendEmailLambda);

        app.synth();
    }
}