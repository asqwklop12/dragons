package com.dragons.interfaces.api.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Google Auth V1 API", description = "구글 소셜 인증 API")
public interface GoogleV1AuthSpec {

  // 구글 인증 페이지로 리다이렉트
  @Operation(summary = "Google 로그인 페이지", description = "구글 인증 페이지로 리다이렉트합니다.")
  void login();

  // Google 콜백 처리
  @Operation(summary = "Google 콜백 처리", description = "구글 인증 콜백 요청을 처리합니다.")
  void callback();
}
