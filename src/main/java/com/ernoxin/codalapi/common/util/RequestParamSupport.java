package com.ernoxin.codalapi.common.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class RequestParamSupport {

    private RequestParamSupport() {
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null) {
                String normalized = value.trim();
                if (!normalized.isEmpty()) {
                    return normalized;
                }
            }
        }

        return "";
    }

    public static String require(String fieldName, String... values) {
        String resolved = firstNonBlank(values);
        if (resolved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameter: " + fieldName);
        }
        return resolved;
    }
}
