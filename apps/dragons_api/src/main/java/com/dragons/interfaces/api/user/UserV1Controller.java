package com.dragons.interfaces.api.user;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Login;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Register;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserV1Controller {
  // 회원가입
  @PostMapping("/register")
  public ApiResponse<UserV1Dto.Register.Response> register(@RequestBody @Validated UserV1Dto.Register.Request request) {

    return ApiResponse.success(new Register.Response(
        "홍길동",
        "user@example.com"
    ));
  }

  // 로그인
  @PostMapping("/login")
  public ApiResponse<UserV1Dto.Login.Response> login(@RequestBody UserV1Dto.Login.Request request) {
    return ApiResponse.success(new Login.Response(
        "아무튼 토큰"
    ));
  }

}
