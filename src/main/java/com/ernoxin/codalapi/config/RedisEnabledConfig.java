package com.ernoxin.codalapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
@Import(RedisAutoConfiguration.class)
public class RedisEnabledConfig {
}
