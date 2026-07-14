package com.ernoxin.codalapi.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "external.codal.request")
public record CodalRequestProperties(
        @Min(0) long minDelayMs,
        @Min(1) int maxConcurrent,
        @Min(0) long retryDelayMs
) {
}
