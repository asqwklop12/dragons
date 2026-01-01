package com.dragons.interfaces.api.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(name = "PostV1Dto", description = "게시글 API v1 DTO 집합")
public class PostV1Dto {

  @Schema(name = "PostCreate", description = "게시글 작성 DTO")
  public static class Create {

    @Schema(name = "PostCreateRequest", description = "게시글 작성 요청")
    public record Request(
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") @NotBlank @Size(min = 1, max = 100) String title,

        @Schema(description = "내용", example = "Spring Security와 JWT를 활용한...") @NotBlank @Size(min = 1, max = 10_000) String content,

        // 카테고리: enum 타입, 필수
        @Schema(description = "카테고리", requiredMode = Schema.RequiredMode.REQUIRED, example = "BACKEND") @NotNull Category category,

        @Schema(description = "공개 여부", example = "true") Boolean isPublic) {
      public Request {
        if (isPublic == null) {
          isPublic = true;
        }
      }
    }

    @Schema(name = "PostCreateResponse", description = "게시글 작성 응답")
    public record Response(
        @Schema(description = "게시글 아이디", example = "1") long id, // 제목
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") String title, // 제목
        @Schema(description = "내용", example = "Spring Security와 JWT를 활용한...") String content, // 내용
        @Schema(description = "카테고리", example = "backend") String category, // 카테고리
        @Schema(description = "작성자", example = "yonghun") String author, // 작가
        @Schema(description = "공개 여부", example = "true") Boolean isPublic // 공개 여부

    ) {

    }
  }

  @Schema(name = "PostSearch", description = "게시글 목록 조회 DTO")
  public static class Search {

    @Schema(name = "PostSearchCondition", description = "게시글 목록 조회 조건")
    public record Condition(
        @Schema(description = "페이지 번호(1부터)", example = "1") @Positive Integer page,
        @Schema(description = "페이지 크기", example = "10") @Positive Integer limit,
        @Schema(description = "정렬 필드", example = "createdAt,desc") String sort) {
      public Condition {
        if (page == null) {
          page = 1;
        }
        if (limit == null) {
          limit = 10;
        }
      }

    }

    @Schema(name = "PostSearchResponse", description = "게시글 목록 조회 응답")
    public record Response(
        @Schema(description = "게시글 목록") List<Posts> posts,
        @Schema(description = "현재 페이지", example = "1") int page,
        @Schema(description = "페이지 크기", example = "10") int size,
        @Schema(description = "총 건수", example = "2") int total

    ) {

      @Schema(name = "PostSummary", description = "게시글 요약 정보")
      public record Posts(
          @Schema(description = "게시글 ID", example = "1") long id, // 아이디
          @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") String title, // 제목
          @Schema(description = "카테고리", example = "backend") String category, // 카테고리
          @Schema(description = "작성자", example = "yonghun") String author // 작가
      ) {
      }

    }

  }

  @Schema(name = "PostGet", description = "게시글 단건 조회 DTO")
  public static class Get {

    @Schema(name = "PostGetResponse", description = "게시글 단건 조회 응답")
    public record Response(
        @Schema(description = "게시글 ID", example = "1") long id,
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") String title, // 제목
        @Schema(description = "내용", example = "Spring Security와 JWT를 활용한...") String content, // 내용
        @Schema(description = "카테고리", example = "backend") String category, // 카테고리
        @Schema(description = "작성자", example = "yonghun") String author // 작가
    ) {

    }
  }

  @Schema(name = "PostUpdate", description = "게시글 수정 DTO")
  public static class Update {

    @Schema(name = "PostUpdateRequest", description = "게시글 수정 요청")
    public record Request(
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") @NotBlank @Size(min = 1, max = 100) String title,
        @Schema(description = "내용", example = "Spring Security와 JWT를 활용한...") @NotBlank @Size(min = 1, max = 10_000) String content) {

    }

    @Schema(name = "PostUpdateResponse", description = "게시글 수정 응답")
    public record Response(
        @Schema(description = "게시글 ID", example = "1") long id,
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") String title,
        @Schema(description = "작성자", example = "yonghun") String author) {

    }
  }

  @Schema(name = "PostDelete", description = "게시글 삭제 DTO")
  public static class Delete {
    @Schema(name = "PostDeleteResponse", description = "게시글 삭제 응답")
    public record Response(
        @Schema(description = "게시글 ID", example = "1") long id,
        @Schema(description = "제목", example = "Spring Boot에서 JWT 인증 구현하기") String title,
        @Schema(description = "작성자", example = "yonghun") String author) {
    }
  }

  @Schema(name = "PostCategory", description = "게시글 카테고리")
  public enum Category {
    BACKEND("backend"), FRONTEND("frontend"), DEVOPS("devops"), ETC("etc");

    private final String name;

    Category(String name) {
      this.name = name;
    }

    @Schema(description = "카테고리 값")
    public String category() {
      return name;
    }


  }
}
