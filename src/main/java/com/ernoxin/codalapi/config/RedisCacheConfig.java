package com.ernoxin.codalapi.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
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

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    GenericJackson2JsonRedisSerializer redisCacheSerializer(ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.ernoxin.codalapi")
                        .allowIfSubType("com.fasterxml.jackson.databind")
                        .allowIfSubType("java.util")
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(cacheObjectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
    CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            CacheProperties cacheProperties,
            GenericJackson2JsonRedisSerializer redisCacheSerializer
    ) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisCacheSerializer))
                .prefixCacheNameWith(cacheProperties.keyPrefix());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "false")
    CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
