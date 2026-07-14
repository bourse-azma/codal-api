package com.ernoxin.codalapi.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "scheduler.notices")
public record CodalNoticeSchedulerProperties(
        boolean enabled,
        @Min(1) long refreshMs,
        @Min(0) long initialDelayMs,
        @Min(1) int targetCount,
        @Min(1) @Max(12) int length,
        @Min(1) int upstreamPageSize
) {
    public int pageCount() {
        return (int) Math.ceil(targetCount / (double) upstreamPageSize);
    }
}
