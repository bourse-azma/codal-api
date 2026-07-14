package com.ernoxin.codalapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate codalRestTemplate(
            RestTemplateBuilder builder,
            CodalThrottleInterceptor throttleInterceptor,
            @Value("${external.codal.base-url}") String baseUrl
    ) {
        RestTemplate restTemplate = builder
                .rootUri(baseUrl)
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
            @Value("${external.codal.web-base-url}") String webBaseUrl
    ) {
        RestTemplate restTemplate = builder
                .rootUri(webBaseUrl)
                .requestFactory(() -> new org.springframework.http.client.BufferingClientHttpRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory()))
                .interceptors(throttleInterceptor, new RandomizedCodalHeadersInterceptor(), new LoggingInterceptor())
                .build();

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(webBaseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        return restTemplate;
    }
}
