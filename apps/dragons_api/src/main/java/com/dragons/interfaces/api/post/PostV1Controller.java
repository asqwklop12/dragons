package com.dragons.interfaces.api.post;

import com.dragons.application.post.PostService;
import com.dragons.application.post.dto.PostDeleteCommand;
import com.dragons.application.post.dto.PostDeleteResult;
import com.dragons.application.post.dto.PostGetCommand;
import com.dragons.application.post.dto.PostGetResult;
import com.dragons.application.post.dto.PostSearchCondition;
import com.dragons.application.post.dto.PostSearchResult;
import com.dragons.application.post.dto.PostUpdateCommand;
import com.dragons.application.post.dto.PostUpdateResult;
import com.dragons.application.post.dto.PostWriteCommand;
import com.dragons.application.post.dto.PostWriteResult;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.post.dto.PostV1Dto;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Create;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Delete.Response;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Get;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Search;
import com.dragons.interfaces.api.post.dto.PostV1Dto.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostV1Controller implements PostV1Spec {

  private final PostService postService;

  // 작성
  @Override
  @PostMapping()
  public ApiResponse<PostV1Dto.Create.Response> create(
      @RequestHeader("X-TOKEN") String token,
      @RequestBody @Validated PostV1Dto.Create.Request request) {

    PostWriteResult result = postService.write(new PostWriteCommand(
        request.title(),
        request.content(),
        request.category().category(),
        request.isPublic(),
        token));

    return ApiResponse.success(new Create.Response(
        result.id(),
        result.title(),
        result.content(),
        result.category(),
        result.author(),
        result.isPublic()));
  }

  // 목록 조회
  @Override
  @GetMapping()
  public ApiResponse<PostV1Dto.Search.Response> search(
      @RequestHeader("X-TOKEN") String token,
      @ModelAttribute PostV1Dto.Search.Condition condition) {

    PostSearchResult result = postService.search(new PostSearchCondition(
        condition.page(),
        condition.limit(),
        condition.sort()));

    List<Search.Response.Posts> posts = result.posts().stream()
        .map(post -> new Search.Response.Posts(
            post.id(),
            post.title(),
            post.category(),
            post.author()))
        .toList();

    return ApiResponse.success(new Search.Response(
        posts,
        result.page(),
        result.size(),
        (int) result.total()));
  }

  // 조회
  @Override
  @GetMapping("/{postId}")
  public ApiResponse<PostV1Dto.Get.Response> get(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId) {

    PostGetResult result = postService.get(new PostGetCommand(postId));

    return ApiResponse.success(new Get.Response(
        result.id(),
        result.title(),
        result.content(),
        result.category(),
        result.author()));
  }

  // 수정
  @Override
  @PutMapping("/{postId}")
  public ApiResponse<PostV1Dto.Update.Response> update(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId,
      @RequestBody @Validated PostV1Dto.Update.Request request) {

    PostUpdateResult result = postService.update(new PostUpdateCommand(
        postId,
        request.title(),
        request.content()));

    return ApiResponse.success(new Update.Response(
        result.id(),
        result.title(),
        result.author()));
  }

  // 삭제
  @Override
  @DeleteMapping("/{postId}")
  public ApiResponse<PostV1Dto.Delete.Response> delete(
      @RequestHeader("X-TOKEN") String token,
      @PathVariable Long postId) {

    PostDeleteResult result = postService.delete(new PostDeleteCommand(postId));

    return ApiResponse.success(new Response(
        result.id(),
        result.title(),
        result.author()));
  }
}
