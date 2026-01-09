package com.dragons.interfaces.api.user;

import com.dragons.application.user.GoogleSocialService;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.support.login.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleV1AuthController implements GoogleV1AuthSpec {

  private final GoogleSocialService googleSocialService;
  private final SessionHelper helper;


  @GetMapping("/url")
  public ApiResponse<String> getGoogleLoginUrl() {
    String googleLoginUrl = googleSocialService.getGoogleLoginUrl();
    return ApiResponse.success(googleLoginUrl);
  }

  // Google 로그인 처리 (Callback Code 수신)
  // 기존 UserV1Controller에 구현했던 POST /login/google과 동일한 역할
  @Override
  @PostMapping("/login")
  public ApiResponse<UserV1Dto.Login.Response> googleLogin(
      @RequestBody @Validated UserV1Dto.GoogleLogin.Request request,
      HttpServletRequest httpRequest) {
    UserLoginResult result = googleSocialService.loginWithGoogle(request.code());
    return processLogin(result, httpRequest);
  }

  // Google 콜백 처리 (GET)
  @Override
  @GetMapping("/callback")
  public String callback(@RequestParam String code, HttpServletRequest httpRequest) {
    return "Google OAuth callback received";
  }

  private ApiResponse<UserV1Dto.Login.Response> processLogin(UserLoginResult result, HttpServletRequest httpRequest) {
    // 세션 처리
    helper.createLoginSession(httpRequest, result.email(), result.loginTime(), "GOOGLE");

    return ApiResponse.success(new UserV1Dto.Login.Response(
        result.email(),
        result.name(),
        result.loginTime()));
  }
}
