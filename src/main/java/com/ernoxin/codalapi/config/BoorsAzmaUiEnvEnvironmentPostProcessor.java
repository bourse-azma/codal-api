package com.ernoxin.codalapi.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class BoorsAzmaUiEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String ENV_PATH_PROPERTY = "boors-azma.ui.env.path";
    private static final String DEFAULT_ENV_PATH = "../boors-azma-ui/.env";
    private static final String PROPERTY_SOURCE_NAME = "boorsAzmaUiDotEnv";

    static Map<String, Object> loadDotEnv(Resource resource) {
        Map<String, Object> properties = new LinkedHashMap<>();
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : content.split("\\R")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separatorIndex = trimmed.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separatorIndex).trim();
                String value = trimmed.substring(separatorIndex + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                properties.put(key, value);
            }
        } catch (IOException ex) {
            return Map.of();
        }
        return properties;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            return;
        }

        for (String candidatePath : resolveCandidatePaths(environment)) {
            Resource resource = new FileSystemResource(candidatePath);
            if (!resource.exists() || !resource.isReadable()) {
                continue;
            }

            Map<String, Object> properties = loadDotEnv(resource);
            if (properties.isEmpty()) {
                continue;
            }

            PropertySource<?> propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
            environment.getPropertySources().addFirst(propertySource);
            return;
        }
    }

    private List<String> resolveCandidatePaths(ConfigurableEnvironment environment) {
        String configuredPath = environment.getProperty(ENV_PATH_PROPERTY, DEFAULT_ENV_PATH);
        return List.of(
                configuredPath,
                Path.of("boors-azma-ui", ".env").toString()
        );
    }
}
