package com.ernoxin.codalapi.common.api;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiResponse<T>(
        String referenceId,
        String timestamp,
        int code,
        String message,
        T result
) {

    public static <T> ApiResponse<T> success(T result) {
        return of(HttpStatus.OK, "success", result);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return of(HttpStatus.OK, message, result);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T result) {
        return new ApiResponse<>(
                MDC.get("referenceId"),
                Instant.now().toString(),
                status.value(),
                message,
                result
        );
    }
}
