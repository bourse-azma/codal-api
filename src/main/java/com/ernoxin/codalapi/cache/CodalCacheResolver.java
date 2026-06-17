package com.ernoxin.codalapi.cache;

import com.ernoxin.codalapi.config.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component("codalCacheResolver")
@RequiredArgsConstructor
public class CodalCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;
    private final CacheProperties cacheProperties;

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Cache cache = cacheManager.getCache(resolveCacheName(context.getMethod().getName()));
        if (cache == null) {
            throw new IllegalStateException("Cache not found for method: " + context.getMethod().getName());
        }
        return List.of(cache);
    }

    private String resolveCacheName(String methodName) {
        return switch (methodName) {
            case "getNotices", "refreshNotices" -> cacheProperties.names().notices();
            case "getCompanies", "refreshCompanies" -> cacheProperties.names().companies();
            case "getIndustryGroups", "refreshIndustryGroups" -> cacheProperties.names().industryGroups();
            case "getCategories", "refreshCategories" -> cacheProperties.names().categories();
            case "getFinancialYears", "refreshFinancialYears" -> cacheProperties.names().financialYears();
            case "getAuditors", "refreshAuditors" -> cacheProperties.names().auditors();
            default -> throw new IllegalStateException("Unsupported cached method: " + methodName);
        };
    }
}
