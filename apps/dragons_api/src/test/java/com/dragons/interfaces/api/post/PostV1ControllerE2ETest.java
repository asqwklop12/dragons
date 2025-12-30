package com.dragons.interfaces.api.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PostV1ControllerE2ETest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @DisplayName("게시글 작성 - 정상 케이스")
  void create_success() throws Exception {
    // given
    String requestBody = """
        {
          "title": "Spring Boot에서 JWT 인증 구현하기",
          "content": "Spring Security와 JWT를 활용한...",
          "category": "BACKEND"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/posts")
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.title").value("Spring Boot에서 JWT 인증 구현하기"))
        .andExpect(jsonPath("$.data.content").value("Spring Security와 JWT를 활용한..."))
        .andExpect(jsonPath("$.data.category").value("backend"))
        .andExpect(jsonPath("$.data.author").value("yonghun"))
        .andExpect(jsonPath("$.data.isPublic").value(true));
  }

  @Test
  @DisplayName("게시글 목록 조회 - 정상 케이스")
  void search_success() throws Exception {
    mockMvc.perform(get("/api/posts")
            .header("X-TOKEN", "dummy-token"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.boards").isArray())
        .andExpect(jsonPath("$.data.boards.length()").value(2))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.size").value(10))
        .andExpect(jsonPath("$.data.total").value(2))
        .andExpect(jsonPath("$.data.boards[0].title").value("Spring Boot에서 JWT 인증 구현하기"))
        .andExpect(jsonPath("$.data.boards[0].category").value("backend"))
        .andExpect(jsonPath("$.data.boards[0].author").value("yonghun"));
  }

  @Test
  @DisplayName("게시글 작성 - 제목 누락")
  void create_fail_missingTitle() throws Exception {
    // given
    String requestBody = """
        {
          "content": "Spring Security와 JWT를 활용한...",
          "category": "BACKEND"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/posts")
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("게시글 작성 - 내용 공백")
  void create_fail_blankContent() throws Exception {
    // given
    String requestBody = """
        {
          "title": "Spring Boot에서 JWT 인증 구현하기",
          "content": " ",
          "category": "BACKEND"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/posts")
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("게시글 작성 - 카테고리 누락")
  void create_fail_missingCategory() throws Exception {
    // given
    String requestBody = """
        {
          "title": "Spring Boot에서 JWT 인증 구현하기",
          "content": "Spring Security와 JWT를 활용한..."
        }
        """;

    // when & then
    mockMvc.perform(post("/api/posts")
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("게시글 단건 조회 - 정상 케이스")
  void get_success() throws Exception {
    mockMvc.perform(get("/api/posts/{postId}", 1)
            .header("X-TOKEN", "dummy-token"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.title").value("Spring Boot에서 JWT 인증 구현하기"))
        .andExpect(jsonPath("$.data.content").value("Spring Security와 JWT를 활용한..."))
        .andExpect(jsonPath("$.data.category").value("backend"))
        .andExpect(jsonPath("$.data.author").value("yonghun"));
  }

  @Test
  @DisplayName("게시글 수정 - 정상 케이스")
  void update_success() throws Exception {
    String requestBody = """
        {
          "title": "Spring Boot에서 JWT 인증 구현하기",
          "content": "Spring Security와 JWT를 활용한..."
        }
        """;

    mockMvc.perform(put("/api/posts/{postId}", 1)
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.title").value("Spring Boot에서 JWT 인증 구현하기"))
        .andExpect(jsonPath("$.data.author").value("yonghun"));
  }

  @Test
  @DisplayName("게시글 수정 - 제목 누락")
  void update_fail_missingTitle() throws Exception {
    String requestBody = """
        {
          "content": "Spring Security와 JWT를 활용한..."
        }
        """;

    mockMvc.perform(put("/api/posts/{postId}", 1)
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("게시글 수정 - 내용 공백")
  void update_fail_blankContent() throws Exception {
    String requestBody = """
        {
          "title": "Spring Boot에서 JWT 인증 구현하기",
          "content": ""
        }
        """;

    mockMvc.perform(put("/api/posts/{postId}", 1)
            .header("X-TOKEN", "dummy-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("게시글 삭제 - 정상 케이스")
  void delete_success() throws Exception {
    mockMvc.perform(delete("/api/posts/{postId}", 1)
            .header("X-TOKEN", "dummy-token"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Spring Boot에서 JWT 인증 구현하기"))
        .andExpect(jsonPath("$.author").value("yonghun"));
  }
}
