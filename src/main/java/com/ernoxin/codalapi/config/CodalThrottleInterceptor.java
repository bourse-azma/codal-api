package com.ernoxin.codalapi.config;

import com.ernoxin.codalapi.client.CodalRequestThrottle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class CodalThrottleInterceptor implements ClientHttpRequestInterceptor {

    private final CodalRequestThrottle throttle;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        CodalRequestThrottle.Permit permit = throttle.acquire();
        try {
            return new ThrottledResponse(execution.execute(request, body), permit);
        } catch (IOException | RuntimeException ex) {
            permit.close();
            throw ex;
        }
    }

    private record ThrottledResponse(
            ClientHttpResponse delegate,
            CodalRequestThrottle.Permit permit
    ) implements ClientHttpResponse {

        @Override
        public HttpStatusCode getStatusCode() throws IOException {
            return delegate.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return delegate.getStatusText();
        }

        @Override
        public HttpHeaders getHeaders() {
            return delegate.getHeaders();
        }

        @Override
        public InputStream getBody() throws IOException {
            return delegate.getBody();
        }

        @Override
        public void close() {
            try {
                delegate.close();
            } finally {
                permit.close();
            }
        }
    }
}
