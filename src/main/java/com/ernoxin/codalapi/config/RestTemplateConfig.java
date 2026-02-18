package com.ernoxin.codalapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate codalRestTemplate(
            RestTemplateBuilder builder,
            @Value("${external.codal.base-url}") String baseUrl,
            @Value("${external.codal.user-agent:Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)}") String userAgent,
            @Value("${external.codal.referer:https://codal.ir/}") String referer,
            @Value("${external.codal.origin:https://codal.ir}") String origin
    ) {
        RestTemplate restTemplate = builder
                .rootUri(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.ALL_VALUE)
                .defaultHeader(HttpHeaders.REFERER, referer)
                .defaultHeader(HttpHeaders.ORIGIN, origin)
                .build();

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        return restTemplate;
    }
}
