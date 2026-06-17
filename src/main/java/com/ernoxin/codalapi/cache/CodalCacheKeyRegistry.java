package com.ernoxin.codalapi.cache;

import com.ernoxin.codalapi.config.CacheProperties;
import com.ernoxin.codalapi.domain.CodalModels;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
public class CodalCacheKeyRegistry {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheProperties cacheProperties;

    public void trackNoticeQuery(CodalModels.NoticeSearchQuery query) {
        try {
            redisTemplate.opsForHash().put(
                    cacheProperties.registry().notices(),
                    CodalCacheKeys.notices(query),
                    objectMapper.writeValueAsString(query)
            );
        } catch (JsonProcessingException ex) {
            log.warn("Failed to track notice cache key: {}", ex.getMessage());
        }
    }

    public List<CodalModels.NoticeSearchQuery> getTrackedNoticeQueries() {
        return redisTemplate.opsForHash().entries(cacheProperties.registry().notices()).values().stream()
                .map(Object::toString)
                .map(this::readNoticeQuery)
                .filter(Objects::nonNull)
                .toList();
    }

    public void trackFinancialYearsSymbol(String symbol) {
        redisTemplate.opsForSet().add(
                cacheProperties.registry().financialYears(),
                CodalCacheKeys.financialYears(symbol)
        );
    }

    public List<String> getTrackedFinancialYearSymbols() {
        return redisTemplate.opsForSet().members(cacheProperties.registry().financialYears()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(symbol -> !symbol.isEmpty())
                .toList();
    }

    private CodalModels.NoticeSearchQuery readNoticeQuery(String payload) {
        try {
            return objectMapper.readValue(payload, CodalModels.NoticeSearchQuery.class);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to read tracked notice cache key: {}", ex.getMessage());
            return null;
        }
    }
}
