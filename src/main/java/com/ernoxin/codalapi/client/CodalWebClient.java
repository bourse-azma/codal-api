package com.ernoxin.codalapi.client;

import com.ernoxin.codalapi.common.exception.UpstreamApiException;
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

/**
 * Fetches CODAL letter pages (HTML) from www.codal.ir. The financial statement data is not exposed as
 * JSON; it is embedded in the HTML page, so the raw HTML is returned and parsed by {@code CodalMapper}.
 */
@Component
@RequiredArgsConstructor
public class CodalWebClient {

    private final RestTemplate codalWebRestTemplate;

    public String getHtml(String path, Map<String, ?> queryParams) {
        return getHtmlWithRetry(buildPath(path, queryParams), 1);
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
                .toUriString();
    }

    private String getHtmlWithRetry(String path, int retriesLeft) {
        try {
            return codalWebRestTemplate.getForObject(path, String.class);
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (retriesLeft > 0 && (statusCode == 400 || statusCode == 429 || statusCode >= 500)) {
                sleepQuietly();
                return getHtmlWithRetry(path, retriesLeft - 1);
            }

            HttpStatus status = HttpStatus.resolve(statusCode);
            throw new UpstreamApiException(
                    "Upstream CODAL page returned status " + statusCode + ": " + ex.getStatusText(),
                    status != null ? status : HttpStatus.BAD_GATEWAY
            );
        } catch (RestClientException ex) {
            if (retriesLeft > 0) {
                sleepQuietly();
                return getHtmlWithRetry(path, retriesLeft - 1);
            }

            throw new UpstreamApiException(
                    "Upstream CODAL page request failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(750L);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
