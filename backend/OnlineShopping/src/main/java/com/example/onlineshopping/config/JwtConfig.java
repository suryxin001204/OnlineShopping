package com.example.onlineshopping.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private Long expiration;

    @PostConstruct
    public void init() {
        log.info("JWT Configuration loaded - Secret: {}, Expiration: {}ms",
                secret != null ? "***" : "NULL",
                expiration);

        if (secret == null || secret.length() < 32) {
            log.warn("JWT secret is not configured properly. Using default development secret.");
            secret = "defaultSecretKeyForDevelopmentOnlyChangeInProductionWithMinimumLength";
        }

        if (expiration == null) {
            expiration = 86400000L; // 24 hours
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}