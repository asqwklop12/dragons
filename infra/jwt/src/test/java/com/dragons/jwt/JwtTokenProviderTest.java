package com.dragons.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.dragons.config.jwt.JwtProperties;
import com.dragons.config.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties(
                "Authorization",
                "dragons-server",
                "this-is-a-very-secure-secret-key-for-testing-purposes-only",
                3600,
                86400);
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void createAccessToken_createsValidToken() {
        String token = jwtTokenProvider.createAccessToken("user-id-123");

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getPayload(token)).isEqualTo("user-id-123");
    }

    @Test
    void createRefreshToken_createsValidToken() {
        String token = jwtTokenProvider.createRefreshToken("user-id-123");

        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getPayload(token)).isEqualTo("user-id-123");
    }

    @Test
    void validateToken_returnsFalseForInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }
}
