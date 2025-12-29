package com.dragons.dragons_api.interfaces.user;

import com.dragons.dragons_api.interfaces.user.dto.UserV1Dto;
import com.dragons.dragons_api.interfaces.user.dto.UserV1Dto.Login;
import com.dragons.dragons_api.interfaces.user.dto.UserV1Dto.Register;
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
  public UserV1Dto.Register.Response register(@RequestBody @Validated UserV1Dto.Register.Request request) {

    return new Register.Response(
        "홍길동",
        "user@example.com"
    );
  }

  // 로그인
  @PostMapping("/login")
  public UserV1Dto.Login.Response login(@RequestBody UserV1Dto.Login.Request request) {
    return new Login.Response(
        "아무튼 토큰"
    );
  }

}
