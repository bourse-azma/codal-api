package com.ernoxin.codalapi.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomizedCodalHeadersInterceptor implements ClientHttpRequestInterceptor {

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15"
    );

    private static final List<String> ACCEPT_LANGUAGES = List.of(
            "fa-IR,fa;q=0.9,en-US;q=0.8,en;q=0.7",
            "en-US,en;q=0.9,fa-IR;q=0.8,fa;q=0.7",
            "fa,en-US;q=0.9,en;q=0.8",
            "en-GB,en;q=0.9,fa-IR;q=0.8"
    );

    private static final List<String> REFERERS = List.of(
            "https://codal.ir/",
            "https://www.codal.ir/",
            "https://codal.ir/ReportList.aspx",
            "https://codal.ir/ReportList.aspx?search"
    );

    @Override
    public ClientHttpResponse intercept(
            org.springframework.http.HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.set(HttpHeaders.USER_AGENT, randomItem(USER_AGENTS));
        headers.set(HttpHeaders.ACCEPT_LANGUAGE, randomItem(ACCEPT_LANGUAGES));
        headers.set(HttpHeaders.REFERER, randomItem(REFERERS));
        headers.set(HttpHeaders.ORIGIN, "https://codal.ir");
        headers.set(HttpHeaders.ACCEPT, String.join(", ",
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.TEXT_PLAIN_VALUE,
                MediaType.ALL_VALUE
        ));
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.set(HttpHeaders.PRAGMA, "no-cache");
        headers.set("DNT", randomBoolean() ? "1" : "0");
        headers.set("Sec-Fetch-Dest", "empty");
        headers.set("Sec-Fetch-Mode", "cors");
        headers.set("Sec-Fetch-Site", "same-site");

        return execution.execute(request, body);
    }

    private static String randomItem(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }

    private static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
