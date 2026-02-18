package com.ernoxin.codalapi.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UpstreamApiException extends RuntimeException {
    private final HttpStatus status;

    public UpstreamApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
