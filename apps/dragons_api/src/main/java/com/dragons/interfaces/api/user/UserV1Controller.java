package com.dragons.interfaces.api.user;

import com.dragons.application.user.UserService;
import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Login;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Register;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserV1Controller implements UserV1Spec {

  private final UserService userService;

  // 회원가입
  @Override
  @PostMapping("/register")
  public ApiResponse<UserV1Dto.Register.Response> register(@RequestBody @Validated UserV1Dto.Register.Request request) {
    var saved = userService.register(new UserRegisterCommand(
        request.name(),
        request.email(),
        request.password()));
    return ApiResponse.success(new Register.Response(
        saved.name(),
        saved.email()));
  }

  // 로그인
  @Override
  @PostMapping("/login")
  public ApiResponse<UserV1Dto.Login.Response> login(@RequestBody @Validated UserV1Dto.Login.Request request,
      HttpServletRequest httpRequest) {
    UserLoginResult result = userService.login(new UserLoginCommand(
        request.email(),
        request.password()));

    // 2. 세션 교체 (기존 세션 무효화 후 새로 생성)
    HttpSession session = httpRequest.getSession(false);
    if (session != null) {
      session.invalidate(); // 기존 세션 정보 파기
    }

    // 신규 세션 생성 및 정보 저장
    session = httpRequest.getSession(true);
    session.setAttribute("userEmail", result.email()); // 나중에 식별을 위해 저장
    session.setAttribute("loginTime", result.loginTime());
    session.setAttribute("provider", "GOOGLE");

    return ApiResponse.success(new Login.Response(
        result.email(),
        result.name(),
        result.loginTime()));
  }

  @Override
  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpServletRequest request) {
    // 1. 현재 요청의 세션을 가져옴 (false: 없으면 새로 만들지 않음)
    HttpSession session = request.getSession(false);

    // 2. 세션이 존재한다면 서버 메모리에서 즉시 파기
    if (session != null) {
      session.invalidate();
    }

    // 3. 성공 응답 (내용물은 빈 값)
    return ApiResponse.success(null);
  }
}
