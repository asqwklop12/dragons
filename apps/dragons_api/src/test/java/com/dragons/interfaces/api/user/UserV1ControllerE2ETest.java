package com.dragons.interfaces.api.user;

import com.dragons.interfaces.api.user.dto.UserV1Dto;
import com.dragons.utils.DragonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DragonIntegrationTest
class UserV1ControllerE2ETest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @DisplayName("회원가입 - 정상 케이스")
  void register_success() throws Exception {
    // given
    UserV1Dto.Register.Request request = new UserV1Dto.Register.Request(
        "홍길동",
        "test@example.com",
        "password123!"
    );

    String requestBody = """
        {
          "name": "홍길동",
          "email": "test@example.com",
          "password": "password123!"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.name").value("홍길동"))
        .andExpect(jsonPath("$.data.email").value("test@example.com"));
  }

  @Test
  @DisplayName("회원가입 - 이름 누락")
  void register_fail_nameBlank() throws Exception {
    // given
    String requestBody = """
        {
          "name": "",
          "email": "test@example.com",
          "password": "password123!"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 - 이름 길이 초과")
  void register_fail_nameTooLong() throws Exception {
    // given
    String requestBody = """
        {
          "name": "aaaaaaaaaaaaaaaaaaaaa",
          "email": "test@example.com",
          "password": "password123!"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 - 잘못된 이메일 형식")
  void register_fail_invalidEmail() throws Exception {
    // given
    String requestBody = """
        {
          "name": "홍길동",
          "email": "invalid-email",
          "password": "password123!"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 - 비밀번호 길이 부족")
  void register_fail_passwordTooShort() throws Exception {
    // given
    String requestBody = """
        {
          "name": "홍길동",
          "email": "test@example.com",
          "password": "pass1"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("회원가입 - 비밀번호 복잡도 미달 (숫자/문자/특수문자 중 2가지 미포함)")
  void register_fail_passwordTooSimple() throws Exception {
    // given
    String requestBody = """
        {
          "name": "홍길동",
          "email": "test@example.com",
          "password": "aaaaaaaa"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("로그인 - 정상 케이스")
  void login_success() throws Exception {
    // given
    String requestBody = """
        {
          "email": "test@example.com",
          "password": "password123!"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"));
  }
}
