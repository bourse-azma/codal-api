package com.ernoxin.codalapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
        boolean enabled,
        long defaultTtlMs,
        String keyPrefix,
        @NestedConfigurationProperty CacheNames names,
        @NestedConfigurationProperty CacheTtls ttls
) {
    public Duration ttlFor(String cacheName) {
        return Duration.ofMillis(ttlMsFor(cacheName));
    }

    public long ttlMsFor(String cacheName) {
        if (cacheName.equals(names().notices())) {
            return ttls().noticesMs();
        }
        if (cacheName.equals(names().noticeSnapshot())) {
            return ttls().noticeSnapshotMs();
        }
        if (cacheName.equals(names().companies())) {
            return ttls().companiesMs();
        }
        if (cacheName.equals(names().industryGroups())) {
            return ttls().industryGroupsMs();
        }
        if (cacheName.equals(names().categories())) {
            return ttls().categoriesMs();
        }
        if (cacheName.equals(names().financialYears())) {
            return ttls().financialYearsMs();
        }
        if (cacheName.equals(names().auditors())) {
            return ttls().auditorsMs();
        }
        if (cacheName.equals(names().financialStatements())) {
            return ttls().financialStatementsMs();
        }
        return defaultTtlMs();
    }

    public record CacheNames(
            String notices,
            String noticeSnapshot,
            String companies,
            String industryGroups,
            String categories,
            String financialYears,
            String auditors,
            String financialStatements
    ) {
    }

    public record CacheTtls(
            long noticesMs,
            long noticeSnapshotMs,
            long companiesMs,
            long industryGroupsMs,
            long categoriesMs,
            long financialYearsMs,
            long auditorsMs,
            long financialStatementsMs
    ) {
    }
}
