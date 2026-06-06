package com.ernoxin.codalapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.debug("Request URI: {}", request.getURI());
        log.debug("Request Method: {}", request.getMethod());
        log.debug("Request Headers: {}", request.getHeaders());
        log.debug("Request Body: {}", new String(body, StandardCharsets.UTF_8));

        ClientHttpResponse response = execution.execute(request, body);

        log.debug("Response Status: {}", response.getStatusCode());
        log.debug("Response Headers: {}", response.getHeaders());
        log.debug("Response Body: {}", StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));

        return response;
    }
}
