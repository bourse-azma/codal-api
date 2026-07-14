package com.ernoxin.codalapi;

import com.ernoxin.codalapi.config.CacheProperties;
import com.ernoxin.codalapi.config.CodalNoticeSchedulerProperties;
import com.ernoxin.codalapi.config.CodalRequestProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@EnableScheduling
@EnableConfigurationProperties({
        CacheProperties.class,
        CodalRequestProperties.class,
        CodalNoticeSchedulerProperties.class
})
@PropertySource(value = "file://${CONFIG}/properties/codal-api.properties", ignoreResourceNotFound = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
