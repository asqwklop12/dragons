package com.dragons.interfaces.api.user;

import com.dragons.application.user.UserService;
import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Login;
import com.dragons.interfaces.api.user.dto.UserV1Dto.Register;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserV1Controller implements UserV1Spec{

  private final UserService userService;

  // 회원가입
  @Override
  @PostMapping("/register")
  public ApiResponse<UserV1Dto.Register.Response> register(@RequestBody @Validated UserV1Dto.Register.Request request) {
    var saved = userService.register(new UserRegisterCommand(
        request.name(),
        request.email(),
        request.password()
    ));
    return ApiResponse.success(new Register.Response(
        saved.name(),
        saved.email()
    ));
  }

  // 로그인
  @Override
  @PostMapping("/login")
  public ApiResponse<UserV1Dto.Login.Response> login(@RequestBody @Validated UserV1Dto.Login.Request request) {

    UserLoginResult result = userService.login(new UserLoginCommand(
        request.email(),
        request.password()
    ));

    return ApiResponse.success(new Login.Response(
        result.token()
    ));
  }

}
