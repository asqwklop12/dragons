package com.dragons.interfaces.api.user;

import com.dragons.application.user.UserService;
import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Login;
import com.dragons.interfaces.api.user.dto.UserV1Dto.MyInfo.Response;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Register;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import com.dragons.support.login.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserV1Controller implements UserV1Spec {

  private final UserService userService;
  private final SessionHelper helper;

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

  @GetMapping("/my")
  @Override
  public ApiResponse<Response> info(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    if (session == null) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "로그인하지 않으셨습니다.");
    }
    String userEmail = (String) session.getAttribute("userEmail");
    ZonedDateTime loginTime = (ZonedDateTime) session.getAttribute("loginTime");
    return ApiResponse.success(new Response(userEmail, loginTime));
  }

  // 로그인
  @Override
  @PostMapping("/login")
  public ApiResponse<UserV1Dto.Login.Response> login(@RequestBody @Validated UserV1Dto.Login.Request request,
                                                     HttpServletRequest httpRequest) {
    UserLoginResult result = userService.login(new UserLoginCommand(
        request.email(),
        request.password()));

    helper.createLoginSession(httpRequest, result.email(), result.loginTime(), "LOCAL");

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
