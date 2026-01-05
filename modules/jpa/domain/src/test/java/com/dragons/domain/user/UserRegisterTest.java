package com.dragons.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    assertThat(user).isNotNull();
    assertThat(user.name()).isEqualTo(name);
    assertThat(user.email()).isEqualTo(email);
    assertThat(user.password()).isEqualTo(password);
  }

  @Nested
  @DisplayName("실패: 이름 검증")
  class NameValidation {
    @Test
    @DisplayName("이름이 비어있으면 IllegalArgumentException")
    void blank_name() {
      assertThatThrownBy(() -> User.register(" \t\n", "test@example.com", "password123"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이름이 100자를 초과하면 IllegalArgumentException")
    void too_long_name() {
      String longName = "a".repeat(101);
      assertThatThrownBy(() -> User.register(longName, "test@example.com", "password123"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("실패: 이메일 검증")
  class EmailValidation {
    @Test
    @DisplayName("이메일이 비어있으면 IllegalArgumentException")
    void blank_email() {
      assertThatThrownBy(() -> User.register("홍길동", "  \n\t", "password123"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이메일 형식이 유효하지 않으면 IllegalArgumentException")
    void invalid_email_format() {
      assertThatThrownBy(() -> User.register("홍길동", "invalid-email", "password123"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("실패: 비밀번호 검증")
  class PasswordValidation {
    @Test
    @DisplayName("비밀번호가 비어있으면 IllegalArgumentException")
    void blank_password() {
      assertThatThrownBy(() -> User.register("홍길동", "test@example.com", " \n\t"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 IllegalArgumentException")
    void short_password() {
      assertThatThrownBy(() -> User.register("홍길동", "test@example.com", "short"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
