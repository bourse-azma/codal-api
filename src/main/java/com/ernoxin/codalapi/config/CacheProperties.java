package com.ernoxin.codalapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@ConfigurationProperties(prefix = "cache")
public record CacheProperties(
        boolean enabled,
        Duration refreshInterval,
        String keyPrefix,
        @NestedConfigurationProperty CacheNames names,
        @NestedConfigurationProperty CacheRegistry registry
) {
    public String noticesName() {
        return names().notices();
    }

    public String companiesName() {
        return names().companies();
    }

    public String industryGroupsName() {
        return names().industryGroups();
    }

    public String categoriesName() {
        return names().categories();
    }

    public String financialYearsName() {
        return names().financialYears();
    }

    public String auditorsName() {
        return names().auditors();
    }

    public String noticesRegistryKey() {
        return registry().notices();
    }

    public String financialYearsRegistryKey() {
        return registry().financialYears();
    }

    public record CacheNames(
            String notices,
            String companies,
            String industryGroups,
            String categories,
            String financialYears,
            String auditors
    ) {
    }

    public record CacheRegistry(
            String notices,
            String financialYears
    ) {
    }
}
