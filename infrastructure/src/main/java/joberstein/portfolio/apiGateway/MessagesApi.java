package joberstein.portfolio.apiGateway;

import com.amazonaws.services.s3.Headers;
import joberstein.portfolio.BaseStack;
import joberstein.portfolio.lambda.SendEmailLambda;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigatewayv2.*;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.lambda.Function;

import java.util.List;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

public class MessagesApi extends BaseStack<HttpApi> {

    private static final String API_NAME = "Messages API";
    private static final String API_DESCRIPTION = "Allows messages to be sent out to a recipient.";
    private static final String MESSAGES_RESOURCE = "/messages";
    private static final String STAGE_NAME = "production";

    private static final String X_AMZ_DATE = Headers.S3_ALTERNATE_DATE;
    private static final String X_AMZ_SECURITY_TOKEN = Headers.SECURITY_TOKEN;
    private static final String X_API_KEY = "X-Api-Key";

    private final Function sendEmailLambda;

    public MessagesApi(Construct scope, Environment environment, SendEmailLambda sendEmailLambda) {
        super(scope, "messagesApi", StackProps.builder()
            .env(environment)
            .build());

        this.addDependency(sendEmailLambda);
        this.sendEmailLambda = sendEmailLambda.getInstance();

        this.init();

        var api = this.getInstance();
        api.addRoutes(buildSendMessagesRoute());
        api.addStage("productionStage", buildStageOptions());
    }
    
    protected HttpApi build() {
        return HttpApi.Builder.create(this, "messagesHttpApi")
            .apiName(API_NAME)
            .description(API_DESCRIPTION)
            .corsPreflight(buildCorsOptions())
            .build();
    }

    private HttpStageOptions buildStageOptions() {
        return HttpStageOptions.builder()
            .stageName(STAGE_NAME)
            .autoDeploy(true)
            .build();
    }

    private CorsPreflightOptions buildCorsOptions() {
        return CorsPreflightOptions.builder()
            .allowMethods(List.of(HttpMethod.POST, HttpMethod.OPTIONS))
            .allowHeaders(List.of(CONTENT_TYPE, AUTHORIZATION, X_AMZ_DATE, X_AMZ_SECURITY_TOKEN, X_API_KEY))
            .allowOrigins(List.of("*"))
            .build();
    }

    private AddRoutesOptions buildSendMessagesRoute() {
        var integration = LambdaProxyIntegration.Builder.create()
            .handler(this.sendEmailLambda)
            .build();

        return AddRoutesOptions.builder()
            .methods(List.of(HttpMethod.POST))
            .path(MESSAGES_RESOURCE)
            .integration(integration)
            .build();
    }
}
