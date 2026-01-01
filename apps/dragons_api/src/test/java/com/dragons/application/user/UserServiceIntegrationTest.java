package com.dragons.application.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.application.user.dto.UserRegisterResult;
import com.dragons.domain.user.UserRepository;
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

}
