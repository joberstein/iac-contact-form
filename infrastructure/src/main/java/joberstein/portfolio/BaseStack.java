package joberstein.portfolio;

import lombok.Getter;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

@Getter
public abstract class BaseStack<T> extends Stack {

    private T instance;

    protected BaseStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);
    }

    protected abstract T build();

    protected void init() {
        this.instance = this.build();
    }

    protected String getContextValue(String key) {
        return (String) this.getNode().tryGetContext(key);
    }
}
