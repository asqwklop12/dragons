package com.dragons.interfaces.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// v1 userDto
public class UserV1Dto {
  // 회원가입
  public static class Register {

    // 요청
    public record Request(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

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
    public record Response(
        String name,
        String email
    ) {

    }
  }

  // 로그인
  public static class Login {

    public record Request(String email, String password) {

    }

    public record Response(String token) {

    }

  }
}
