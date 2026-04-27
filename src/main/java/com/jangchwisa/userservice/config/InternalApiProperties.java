package com.jangchwisa.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record InternalApiProperties(
        String internalApiKey,
        String internalApiHeaderName
) {
}
