package com.dragons.interfaces.api.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class PostV1Dto {

  public static class Create {

    public record Request(
        @NotBlank
        @Size(min = 1, max = 100)
        String title,
        @NotBlank
        @Size(min = 1, max = 10_000)
        String content,
        // 카테고리: enum 타입, 필수
        @NotNull
        Category category,
        Boolean isPublic
    ) {
      public Request {
        if (isPublic == null) {
          isPublic = true;
        }
      }
    }

    public record Response(
        String title, // 제목
        String content, //내용
        String category, // 카테고리
        String author, // 작가
        Boolean isPublic // 공개 여부

    ) {

    }
  }

  public static class Search {

    public record Condition(
        Integer page,
        Integer limit,
        String sort
    ) {
      public Condition {
        if (page == null) {
          page = 1;
        }
        if (limit == null) {
          limit = 10;
        }
      }

    }

    public record Response(
        List<Posts> boards,
        int page,
        int size,
        int total

    ) {

      public record Posts(
          long id,      //아이디
          String title, // 제목
          String category, // 카테고리
          String author // 작가
      ) {
      }

    }

  }

  public static class Get {

    public record Response(
        long id,
        String title, // 제목
        String content, //내용
        String category, // 카테고리
        String author // 작가
    ) {

    }
  }

  public static class Update {

    public record Request(
        @NotBlank
        @Size(min = 1, max = 100)
        String title,
        @NotBlank
        @Size(min = 1, max = 10_000)
        String content
    ) {

    }

    public record Response(
        long id,
        String title,
        String author
    ) {

    }
  }

  public static class Delete {
    public record Response(
        int id,
        String title,
        String author
    ) {
    }
  }

  enum Category {
    BACKEND("backend"), FRONTEND("frontend"), DEVOPS("devops"), ECT("ect");

    private final String name;

    Category(String name) {
      this.name = name;
    }

    public String category() {
      return name;
    }


  }
}
