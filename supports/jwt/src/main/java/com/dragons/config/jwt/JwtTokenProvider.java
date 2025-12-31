package com.dragons.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.clientSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String email) {
        return createToken(email, jwtProperties.accessTokenValidityInSeconds());
    }

    public String createRefreshToken(String email) {
        return createToken(email, jwtProperties.refreshTokenValidityInSeconds());
    }

    private String createToken(String email, long validityInSeconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInSeconds * 1000);

        return Jwts.builder()
                .subject(email)
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String getPayload(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
