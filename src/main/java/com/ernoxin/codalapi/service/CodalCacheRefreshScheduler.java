package com.ernoxin.codalapi.service;

import com.ernoxin.codalapi.cache.CodalCacheKeyRegistry;
import com.ernoxin.codalapi.cache.CodalCacheKeys;
import com.ernoxin.codalapi.config.CacheProperties;
import com.ernoxin.codalapi.domain.CodalModels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class CodalCacheRefreshScheduler {

    private final TaskScheduler taskScheduler;
    private final CacheProperties cacheProperties;
    private final CacheManager cacheManager;
    private final CodalService codalService;
    private final CodalCacheKeyRegistry cacheKeyRegistry;

    @PostConstruct
    void scheduleRefresh() {
        taskScheduler.scheduleWithFixedDelay(this::refreshCachedData, java.time.Instant.now(), cacheProperties.refreshInterval());
        log.info("Scheduled Codal cache refresh every {}", cacheProperties.refreshInterval());
    }

    void refreshCachedData() {
        refreshReferenceDataIfCached();
        refreshTrackedNotices();
        refreshTrackedFinancialYears();
    }

    private void refreshReferenceDataIfCached() {
        refreshIfCached(cacheProperties.names().companies(), "all", codalService::refreshCompanies);
        refreshIfCached(cacheProperties.names().industryGroups(), "all", codalService::refreshIndustryGroups);
        refreshIfCached(cacheProperties.names().categories(), "all", codalService::refreshCategories);
        refreshIfCached(cacheProperties.names().auditors(), "all", codalService::refreshAuditors);
    }

    private void refreshIfCached(String cacheName, String key, Runnable refreshAction) {
        if (!isCached(cacheName, key)) {
            return;
        }
        refreshSafely(cacheName, refreshAction);
    }

    private void refreshTrackedNotices() {
        for (CodalModels.NoticeSearchQuery query : cacheKeyRegistry.getTrackedNoticeQueries()) {
            String cacheKey = CodalCacheKeys.notices(query);
            if (!isCached(cacheProperties.names().notices(), cacheKey)) {
                continue;
            }
            refreshSafely("notices:" + query.symbol(), () -> codalService.refreshNotices(query));
        }
    }

    private void refreshTrackedFinancialYears() {
        for (String symbol : cacheKeyRegistry.getTrackedFinancialYearSymbols()) {
            String cacheKey = CodalCacheKeys.financialYears(symbol);
            if (!isCached(cacheProperties.names().financialYears(), cacheKey)) {
                continue;
            }
            refreshSafely("financial-years:" + symbol, () -> codalService.refreshFinancialYears(symbol));
        }
    }

    private boolean isCached(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache != null && cache.get(key) != null;
    }

    private void refreshSafely(String cacheName, Runnable refreshAction) {
        try {
            refreshAction.run();
            log.debug("Refreshed Codal cache entry: {}", cacheName);
        } catch (RuntimeException ex) {
            log.warn("Failed to refresh Codal cache entry {}: {}", cacheName, ex.getMessage());
        }
    }
}
