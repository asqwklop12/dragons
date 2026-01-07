package com.dragons.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.slf4j.*;

@Component
public class JwtTokenProvider {
  private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
  private final JwtProperties jwtProperties;
  private final SecretKey key;

  public JwtTokenProvider(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    if (jwtProperties.clientSecret().getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException("JWT clientSecret은 최소 32바이트 이상이어야 합니다.");
    }
    this.key = Keys.hmacShaKeyFor(jwtProperties.clientSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(String subject) {
    return createToken(subject, jwtProperties.accessTokenValidityInSeconds());
  }


  public String createRefreshToken(String subject) {
    return createToken(subject, jwtProperties.refreshTokenValidityInSeconds());
  }

  private String createToken(String subject, long validityInSeconds) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInSeconds * 1000);

    return Jwts.builder()
        .subject(subject)
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
      log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
      return false;
    }
  }
}
