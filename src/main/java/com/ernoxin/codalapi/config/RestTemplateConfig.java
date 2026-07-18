package com.ernoxin.codalapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate codalRestTemplate(
            RestTemplateBuilder builder,
            CodalThrottleInterceptor throttleInterceptor,
            @Value("${external.codal.base-url}") String baseUrl,
            @Value("${external.codal.connect-timeout-ms:10000}") long connectTimeoutMs,
            @Value("${external.codal.read-timeout-ms:30000}") long readTimeoutMs
    ) {
        RestTemplate restTemplate = builder
                .rootUri(baseUrl)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .requestFactory(() -> new org.springframework.http.client.BufferingClientHttpRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory()))
                .interceptors(throttleInterceptor, new RandomizedCodalHeadersInterceptor(), new LoggingInterceptor())
                .build();

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        return restTemplate;
    }

    @Bean
    public RestTemplate codalWebRestTemplate(
            RestTemplateBuilder builder,
            CodalThrottleInterceptor throttleInterceptor,
            @Value("${external.codal.web-base-url}") String webBaseUrl,
            @Value("${external.codal.connect-timeout-ms:10000}") long connectTimeoutMs,
            @Value("${external.codal.read-timeout-ms:30000}") long readTimeoutMs
    ) {
        RestTemplate restTemplate = builder
                .rootUri(webBaseUrl)
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .requestFactory(() -> new org.springframework.http.client.BufferingClientHttpRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory()))
                .interceptors(throttleInterceptor, new RandomizedCodalHeadersInterceptor(), new LoggingInterceptor())
                .build();

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(webBaseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        return restTemplate;
    }
}
