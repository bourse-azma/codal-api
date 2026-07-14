package com.ernoxin.codalapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    GenericJackson2JsonRedisSerializer redisCacheSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            CacheProperties cacheProperties,
            GenericJackson2JsonRedisSerializer redisCacheSerializer
    ) {
        RedisCacheConfiguration baseConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisCacheSerializer))
                .prefixCacheNameWith(cacheProperties.keyPrefix())
                .entryTtl(Duration.ofMillis(cacheProperties.defaultTtlMs()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = Stream.of(
                        cacheProperties.names().notices(),
                        cacheProperties.names().noticeSnapshot(),
                        cacheProperties.names().companies(),
                        cacheProperties.names().industryGroups(),
                        cacheProperties.names().categories(),
                        cacheProperties.names().financialYears(),
                        cacheProperties.names().auditors(),
                        cacheProperties.names().financialStatements()
                )
                .distinct()
                .collect(java.util.stream.Collectors.toMap(
                        cacheName -> cacheName,
                        cacheName -> baseConfiguration.entryTtl(cacheProperties.ttlFor(cacheName))
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "false")
    CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
