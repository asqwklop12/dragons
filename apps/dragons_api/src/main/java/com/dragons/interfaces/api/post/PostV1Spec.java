package com.dragons.interfaces.api.post;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.post.dto.PostV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Post V1 API", description = "게시글 관리 API")
public interface PostV1Spec {

  @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
  ApiResponse<PostV1Dto.Create.Response> create(String token, PostV1Dto.Create.Request request);

  @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 페이징으로 조회합니다.")
  ApiResponse<PostV1Dto.Search.Response> search(String token, PostV1Dto.Search.Condition condition);

  @Operation(summary = "게시글 단건 조회", description = "게시글을 식별자로 조회합니다.")
  ApiResponse<PostV1Dto.Get.Response> get(String token, Long postId);

  @Operation(summary = "게시글 수정", description = "게시글 제목/내용을 수정합니다.")
  ApiResponse<PostV1Dto.Update.Response> update(String token, Long postId, PostV1Dto.Update.Request request);

  @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
  ApiResponse<PostV1Dto.Delete.Response> delete(String token, Long postId);
}
