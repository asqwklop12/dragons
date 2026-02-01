package com.dragons.client;

import com.dragons.domain.social.GoogleOAuthClient;
import com.dragons.domain.social.GoogleOAuthResponse;
import com.dragons.executor.RetryExecutor;
import java.util.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class GoogleOAuthClientImpl implements GoogleOAuthClient {

  private final RestTemplate restTemplate;
  private final String clientId;
  private final String clientSecret;
  private final RetryExecutor retryExecutor;
  private final String port;

  public GoogleOAuthClientImpl(RestTemplate restTemplate,
                               @Qualifier("socialRetryExecutor") RetryExecutor retryExecutor,
                               @Value("${google.client-id}") String clientId,
                               @Value("${google.client-secret}") String clientSecret,
                               Environment environment) {
    this.retryExecutor = retryExecutor;
    this.restTemplate = restTemplate;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.port = environment.getProperty("server.port");;
  }

  @Override
  public String getGoogleLoginUrl() {
    String loginUrl = UriComponentsBuilder
        .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", "http://localhsot:" + port + "/api/auth/google/callback")
        .queryParam("response_type", "code")
        .queryParam("scope", "email profile").build()
        .toUriString();
    log.debug("Generated Google login URL: {}", loginUrl);
    return loginUrl;
  }

  @Override
  public GoogleOAuthResponse getUserInfo(String code) {
    log.info("Starting Google OAuth process with authorization code");

    // 1. Authorization Code를 Access Token으로 교환
    String accessToken = retryExecutor.execute(() -> exchangeCodeForToken(code));

    // 2. Access Token으로 사용자 정보 가져오기
    return retryExecutor.execute(() -> fetchUserInfo(accessToken));
  }

  private String exchangeCodeForToken(String code) {
    String tokenUrl = "https://oauth2.googleapis.com/token";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", "http://localhsot:" + port + "/api/auth/google/callback");
    params.add("grant_type", "authorization_code");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    // 파라미터와 헤더를 합친 HttpEntity 생성
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    log.debug("Requesting access token from Google");

    ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
        tokenUrl,
        request,
        GoogleTokenResponse.class);

    GoogleTokenResponse tokenResponse = response.getBody();

    if (tokenResponse == null || tokenResponse.accessToken() == null) {
      log.error("Invalid token response from Google: response is null or missing access_token");
      throw new RuntimeException("Google OAuth 토큰 응답이 유효하지 않습니다");
    }

    log.info("Successfully obtained access token from Google");
    return tokenResponse.accessToken();
  }

  private GoogleOAuthResponse fetchUserInfo(String accessToken) {
    String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    log.debug("Fetching user info from Google");

    ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
        userInfoUrl,
        HttpMethod.GET,
        entity,
        GoogleUserInfoResponse.class);

    GoogleUserInfoResponse userInfo = response.getBody();

    if (userInfo == null) {
      log.error("User info response from Google is null");
      throw new RuntimeException("Google 사용자 정보를 가져올 수 없습니다");
    }

    if (userInfo.email() == null || userInfo.email().isBlank()) {
      log.error("Email is missing in Google user info response");
      throw new RuntimeException("Google 계정에서 이메일을 가져올 수 없습니다");
    }

    log.info("Successfully fetched user info for email: {}", userInfo.email());

    return new GoogleOAuthResponse(
        userInfo.email(),
        userInfo.name());
  }
}
