package com.ernoxin.codalapi.client;

import com.ernoxin.codalapi.common.exception.UpstreamApiException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CodalClient {

    private final RestTemplate codalRestTemplate;

    public JsonNode get(String path) {
        return get(path, Map.of());
    }

    public JsonNode get(String path, Map<String, ?> queryParams) {
        return getJson(buildPath(path, queryParams));
    }

    private String buildPath(String path, Map<String, ?> queryParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        queryParams.forEach((key, value) -> {
            if (value != null) {
                String textValue = String.valueOf(value).trim();
                if (!textValue.isEmpty()) {
                    params.add(key, textValue);
                }
            }
        });

        return UriComponentsBuilder.fromPath(path)
                .queryParams(params)
                .build()
                .encode()
                .toUriString();
    }

    private JsonNode getJson(String path) {
        return getJsonWithRetry(path, 1);
    }

    private JsonNode getJsonWithRetry(String path, int retriesLeft) {
        try {
            return codalRestTemplate.getForObject(path, JsonNode.class);
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (retriesLeft > 0 && (statusCode == 400 || statusCode == 429 || statusCode >= 500)) {
                try {
                    Thread.sleep(750L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return getJsonWithRetry(path, retriesLeft - 1);
            }

            HttpStatus status = HttpStatus.resolve(statusCode);
            throw new UpstreamApiException(
                    "Upstream API returned status " + statusCode + ": " + ex.getStatusText(),
                    status != null ? status : HttpStatus.BAD_GATEWAY
            );
        } catch (RestClientException ex) {
            if (retriesLeft > 0) {
                try {
                    Thread.sleep(750L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return getJsonWithRetry(path, retriesLeft - 1);
            }

            throw new UpstreamApiException(
                    "Upstream API request failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}
