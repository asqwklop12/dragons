package com.dragons.interfaces.api.user;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Google Auth V1 API", description = "구글 소셜 인증 API")
public interface GoogleV1AuthSpec {

  // Google 로그인 (Code 수신)
  @Operation(summary = "Google 로그인", description = "구글 인증 코드를 받아 로그인을 처리합니다.")
  ApiResponse<UserV1Dto.Login.Response> googleLogin(@RequestBody @Validated UserV1Dto.GoogleLogin.Request request,
      HttpServletRequest httpRequest);

  // Google 콜백 처리 (GET) - 테스트용: 코드를 화면에 출력
  @Hidden
  @Operation(summary = "Google 콜백 처리", description = "구글 인증 후 리다이렉트되는 콜백 엔드포인트입니다. (테스트용: 코드 반환)")
  void callback(HttpServletResponse response, @RequestParam String code);
}
