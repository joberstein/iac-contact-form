package joberstein.portfolio.http;

import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.apache.http.HttpHeaders;

import kong.unirest.HttpMethod;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import kong.unirest.UnirestParsingException;
import kong.unirest.jackson.JacksonObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UnirestClient implements HttpClient {
    
    private UnirestInstance unirestClient;
    private LambdaLogger logger;
    
    public static HttpClient newInstance(LambdaLogger logger) {
        UnirestInstance instance = Unirest.spawnInstance();
        instance.config().setObjectMapper(new JacksonObjectMapper());
        return new UnirestClient(instance, logger);
    }

    /**
     * Performs a post with the given uri, query param map, and application/json header.
     * The response class provided is the response body class type.
     * @return the response body of the POST request. If there was an error, the body will be null.
     */
    public <T> T postForObject(String uri, Map<String,Object> params, Class<T> responseClass) {        
        var response = unirestClient.post(uri)
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .queryString(params)
            .asObject(responseClass)
            .ifFailure(failedResponse -> {
                logResponse(HttpMethod.POST, buildUrl(uri, params), failedResponse);
                failedResponse.getParsingError().ifPresent(this::logParsingException);
            });
        
        return response.getBody();
    }

    public boolean isRunning() {
        return unirestClient.isRunning();
    }

    public void close() {
        unirestClient.shutDown();
    }

    private <T> void logResponse(HttpMethod method, String url, HttpResponse<T> response) {
        logger.log(String.format("%s|%d|%s", method.name(), url, response.getStatus()));
    }

    private void logParsingException(UnirestParsingException e) {
        logger.log(String.format("Failed to parse response: %s", e.getMessage()));
        logger.log(String.format("Failed response body: %s", e.getOriginalBody()));
    }

    private String buildUrl(String uri, Map<String,Object> params) {
        String queryString = params.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));

        return uri + "?" + queryString;
    }
}
