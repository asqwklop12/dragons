package com.dragons.interfaces.api.post;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.post.dto.PostV1Dto;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Create;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Delete.Response;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Get;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Search;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Update;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostV1Controller implements PostV1Spec {


  // 작성
  @Override
  @PostMapping()
  public ApiResponse<PostV1Dto.Create.Response> create(
      @RequestHeader("X-TOKEN") String token,
      @RequestBody @Validated PostV1Dto.Create.Request request) {

    return ApiResponse.success(new Create.Response(
        "Spring Boot에서 JWT 인증 구현하기",
        "Spring Security와 JWT를 활용한...",
        "backend",
        "yonghun",
        true
    ));
  }

  // 목록 조회
  @Override
  @GetMapping()
  public ApiResponse<PostV1Dto.Search.Response> search(
      @RequestHeader("X-TOKEN") String token,
      @ModelAttribute PostV1Dto.Search.Condition condition) {

    return ApiResponse.success(new Search.Response(
        List.of(
            new Search.Response.Posts(
                1L,
                "Spring Boot에서 JWT 인증 구현하기",
                "backend",
                "yonghun"
            ),
            new Search.Response.Posts(
                2L,
                "Spring Boot에서 JWT 인증 구현하기2",
                "backend",
                "yonghun2"
            )
        ), 1, 10, 2));
  }

  // 조회
  @Override
  @GetMapping("/{postId}")
  public ApiResponse<PostV1Dto.Get.Response> get(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId) {

    return ApiResponse.success(new Get.Response(
        1L,
        "Spring Boot에서 JWT 인증 구현하기",
        "Spring Security와 JWT를 활용한...",
        "backend",
        "yonghun"
    ));
  }

  // 수정
  @Override
  @PutMapping("/{postId}")
  public ApiResponse<PostV1Dto.Update.Response> update(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId,
      @RequestBody @Validated PostV1Dto.Update.Request request) {

    return ApiResponse.success(new Update.Response(1L,
        "Spring Boot에서 JWT 인증 구현하기",
        "yonghun"));
  }


  // 삭제
  @Override
  @DeleteMapping("/{postId}")
  public PostV1Dto.Delete.Response delete(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId) {

    return new Response(
        1,
        "Spring Boot에서 JWT 인증 구현하기",
        "yonghun"
    );
  }
}
