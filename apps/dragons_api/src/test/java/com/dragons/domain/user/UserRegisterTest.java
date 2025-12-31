package com.dragons.domain.user;

import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRegisterTest {

  @Test
  @DisplayName("성공: 올바른 값으로 회원을 등록하면 User가 생성된다")
  void register_success() {
    // given
    String name = "홍길동";
    String email = "test@example.com";
    String password = "password123";

    // when
    User user = User.register(name, email, password);

    // then
    assertAll(
        () -> assertThat(user).isNotNull(),
        () -> assertEquals(name, user.name()),
        () -> assertEquals(email, user.email()),
        () -> assertEquals(password, user.password())
    );
  }

  @Nested
  @DisplayName("실패: 이름 검증")
  class NameValidation {
    @Test
    @DisplayName("이름이 비어있으면 BAD_REQUEST")
    void blank_name() {
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register(" \t\n", "test@example.com", "password123"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }

    @Test
    @DisplayName("이름이 100자를 초과하면 BAD_REQUEST")
    void too_long_name() {
      String longName = "a".repeat(101);
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register(longName, "test@example.com", "password123"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }
  }

  @Nested
  @DisplayName("실패: 이메일 검증")
  class EmailValidation {
    @Test
    @DisplayName("이메일이 비어있으면 BAD_REQUEST")
    void blank_email() {
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register("홍길동", "  \n\t", "password123"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }

    @Test
    @DisplayName("이메일 형식이 유효하지 않으면 BAD_REQUEST")
    void invalid_email_format() {
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register("홍길동", "invalid-email", "password123"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }
  }

  @Nested
  @DisplayName("실패: 비밀번호 검증")
  class PasswordValidation {
    @Test
    @DisplayName("비밀번호가 비어있으면 BAD_REQUEST")
    void blank_password() {
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register("홍길동", "test@example.com", " \n\t"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 BAD_REQUEST")
    void short_password() {
      CoreException ex = assertThrows(CoreException.class,
          () -> User.register("홍길동", "test@example.com", "short"));
      assertEquals(ErrorType.BAD_REQUEST, ex.getErrorType());
    }
  }
}
