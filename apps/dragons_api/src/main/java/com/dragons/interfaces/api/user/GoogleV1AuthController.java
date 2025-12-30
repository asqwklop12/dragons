package com.dragons.interfaces.api.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleV1AuthController {
  // 구글 인증 페이지로 리다이렉트
  @GetMapping
  public void login() {

  }

  // Google 콜백 처리
  @GetMapping("/callback")
  public void callback() {

  }
}
