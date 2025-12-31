package com.dragons.application.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.application.user.dto.UserRegisterResult;
import com.dragons.domain.user.UserRepository;
import com.dragons.support.error.CoreException;
import com.dragons.utils.DatabaseCleanUp;
import com.dragons.utils.DragonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DragonIntegrationTest
class UserServiceIntegrationTest {

  @Autowired
  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DatabaseCleanUp databaseCleanup;

  @BeforeEach
  void setUp() {
    databaseCleanup.truncateAllTables();
  }

  @Test
  @DisplayName("회원가입 성공")
  void register_success() {
    // given
    UserRegisterCommand command = new UserRegisterCommand(
        "홍길동",
        "test@example.com",
        "password123!");

    // when
    UserRegisterResult result = userService.register(command);

    // then
    assertThat(result.name()).isEqualTo("홍길동");
    assertThat(result.email()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    // given
    userRepository.save(com.dragons.domain.user.User.register("홍길동", "test@example.com", "password123!"));
    UserLoginCommand command = new UserLoginCommand("test@example.com", "password123!");

    // when
    UserLoginResult result = userService.login(command);

    // then
    assertThat(result.token()).isNotNull();
  }

  @Test
  @DisplayName("로그인 성공")
  void login_success_2() {
    // given
    userRepository.save(com.dragons.domain.user.User.register("홍길동", "test@example.com", "password123!"));
    UserLoginCommand command = new UserLoginCommand("test@example.com", "password123!");

    // when
    UserLoginResult result = userService.login(command);

    // then
    assertThat(result.token()).isNotNull();
  }

  @Test
  @DisplayName("회원가입 실패 - 이름 누락")
  void register_fail_nameBlank() {
    // given
    UserRegisterCommand command = new UserRegisterCommand(
        "",
        "test@example.com",
        "password123!");

    // when & then
    assertThatThrownBy(() -> userService.register(command))
        .isInstanceOf(CoreException.class)
        .hasMessage("이름은 비어 있을 수 없습니다.");
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 형식 오류")
  void register_fail_invalidEmail() {
    // given
    UserRegisterCommand command = new UserRegisterCommand(
        "홍길동",
        "invalid-email",
        "password123!");

    // when & then
    assertThatThrownBy(() -> userService.register(command))
        .isInstanceOf(CoreException.class)
        .hasMessage("유효한 이메일 형식이 아닙니다.");
  }

  @Test
  @DisplayName("회원가입 실패 - 비밀번호 길이 부족")
  void register_fail_passwordTooShort() {
    // given
    UserRegisterCommand command = new UserRegisterCommand(
        "홍길동",
        "test@example.com",
        "pass");

    // when & then
    assertThatThrownBy(() -> userService.register(command))
        .isInstanceOf(CoreException.class)
        .hasMessage("비밀번호는 최소 8자 이상이어야 합니다.");
  }
}
