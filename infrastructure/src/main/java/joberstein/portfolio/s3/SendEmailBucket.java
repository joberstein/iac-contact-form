package joberstein.portfolio.s3;

import joberstein.portfolio.BaseStack;
import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.s3.*;

import java.util.List;

public class SendEmailBucket extends BaseStack<Bucket> {

    public static final String BUCKET_NAME = "send-email-assets";

    public SendEmailBucket(Construct scope, Environment environment) {
        super(scope, "sendEmailBucket", StackProps.builder()
                .env(environment)
                .build());

        this.init();
    }

    @Override
    protected Bucket build() {
        return Bucket.Builder.create(this, "sendEmailBucket")
            .bucketName(BUCKET_NAME)
            .versioned(true)
            .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
            .lifecycleRules(getLifecycleRules())
            .removalPolicy(RemovalPolicy.DESTROY)
            .build();
    }

    private List<LifecycleRule> getLifecycleRules() {
        return List.of(
            LifecycleRule.builder()
                .noncurrentVersionExpiration(Duration.days(30))
                .noncurrentVersionTransitions(getNonCurrentVersionTransitions())
                .build()
        );
    }

    private List<NoncurrentVersionTransition> getNonCurrentVersionTransitions() {
        return List.of(
            NoncurrentVersionTransition.builder()
                .storageClass(StorageClass.GLACIER)
                .transitionAfter(Duration.days(1))
                .build()
        );
    }
}