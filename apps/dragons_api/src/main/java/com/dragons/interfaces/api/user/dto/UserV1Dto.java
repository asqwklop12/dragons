package com.dragons.interfaces.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// v1 userDto
@Schema(name = "UserV1Dto", description = "사용자(Auth) API v1 DTO 집합")
public class UserV1Dto {
  // 회원가입
  @Schema(name = "UserRegister", description = "회원가입 DTO")
  public static class Register {

    // 요청
    @Schema(name = "UserRegisterRequest", description = "회원가입 요청")
    public record Request(

        @Schema(description = "이름", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
        String name,

        @Schema(description = "이메일", example = "test@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호(영문/숫자/특수문자 중 2가지 이상 포함)", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        @Pattern(
            regexp = "^(?:(?=.*[A-Za-z])(?=.*\\d)|(?=.*[A-Za-z])(?=.*[^A-Za-z0-9])|(?=.*\\d)(?=.*[^A-Za-z0-9]))[A-Za-z\\d[^A-Za-z0-9]]+$",
            message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 포함해야 합니다."
        )
        String password
    ) {


    }

    // 응답
    @Schema(name = "UserRegisterResponse", description = "회원가입 응답")
    public record Response(
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "이메일", example = "user@example.com")
        String email
    ) {

    }
  }

  // 로그인
  @Schema(name = "UserLogin", description = "로그인 DTO")
  public static class Login {

    @Schema(name = "UserLoginRequest", description = "로그인 요청")
    public record Request(
        @Schema(description = "이메일", example = "test@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        @Pattern(
            regexp = "^(?:(?=.*[A-Za-z])(?=.*\\d)|(?=.*[A-Za-z])(?=.*[^A-Za-z0-9])|(?=.*\\d)(?=.*[^A-Za-z0-9]))[A-Za-z\\d[^A-Za-z0-9]]+$",
            message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 포함해야 합니다."
        )
        String password
    ) {

    }

    @Schema(name = "UserLoginResponse", description = "로그인 응답")
    public record Response(
        @Schema(description = "인증 토큰", example = "아무튼 토큰") String token
    ) {

    }

  }
}
