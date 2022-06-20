package joberstein.portfolio.http;

import java.util.Map;

public interface HttpClient {

    <T> T postForObject(String uri, Map<String,Object> params, Class<T> responseClass);
    boolean isRunning();
    void close();
}
