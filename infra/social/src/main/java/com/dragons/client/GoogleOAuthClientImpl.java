package com.dragons.client;

import com.dragons.domain.social.GoogleOAuthClient;
import com.dragons.domain.social.GoogleOAuthResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthClientImpl implements GoogleOAuthClient {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${google.client-id}")
  private String clientId;

  @Value("${google.client-secret}")
  private String clientSecret;

  @Value("${google.redirect-uri}")
  private String redirectUri;

  public GoogleOAuthResponse getUserEmail(String code) {
    // 1. Authorization Code를 Access Token으로 교환
    String tokenUrl = "https://oauth2.googleapis.com/token";

    // Google API 스펙에 맞춘 파라미터 구성
    Map<String, String> params = Map.of(
        "code", code,
        "client_id", clientId,
        "client_secret", clientSecret,
        "redirect_uri", redirectUri,
        "grant_type", "authorization_code"
    );

    try {
      // 토큰 요청
      Map<String, Object> response = restTemplate.postForObject(tokenUrl, params, Map.class);
      String accessToken = (String) response.get("access_token");

      // 2. Access Token으로 구글 유저 정보(Resource) 가져오기
      String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

      // 헤더에 Bearer 토큰 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
          userInfoUrl,
          HttpMethod.GET,
          entity,
          Map.class
      );

      Map<String, Object> userInfo = userInfoResponse.getBody();
      return
          new GoogleOAuthResponse(
              (String) userInfo.get("email"),
              (String) userInfo.get("name")
          );

    } catch (Exception e) {
      // 통신 실패 시 예외 처리 (CoreException 등으로 감싸서 던지는 것을 권장)
      throw new RuntimeException("구글 로그인 중 오류가 발생했습니다: " + e.getMessage());
    }
  }
}
