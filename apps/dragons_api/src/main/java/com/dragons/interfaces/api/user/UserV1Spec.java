package com.dragons.interfaces.api.user;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Register.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth V1 API", description = "인증/사용자 계정 API")
public interface UserV1Spec {

  // 회원가입
  @Operation(
      summary = "회원가입",
      description = "새 계정을 등록합니다."
  )
  ApiResponse<Response> register(
      @Schema(name = "회원가입 요청", description = "등록할 사용자 계정 정보")
      UserV1Dto.Register.Request request);

  // 로그인
  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 인증합니다."
  )
  ApiResponse<UserV1Dto.Login.Response> login(
      @Schema(name = "로그인 요청", description = "이메일과 비밀번호")
      UserV1Dto.Login.Request request);

}
