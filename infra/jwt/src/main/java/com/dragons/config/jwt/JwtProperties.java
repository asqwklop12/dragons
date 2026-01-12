package com.dragons.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String header,
        String issuer,
        String clientSecret,
        long accessTokenValidityInSeconds,
        long refreshTokenValidityInSeconds) {
}
