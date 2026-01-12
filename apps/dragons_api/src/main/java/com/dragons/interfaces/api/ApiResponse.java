package com.dragons.interfaces.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiResponse", description = "공통 응답 래퍼. 모든 API 응답은 meta + data 구조를 가집니다.")
public record ApiResponse<T>(
    @Schema(description = "메타데이터(결과/오류 코드/메시지)") Metadata meta,
    @Schema(description = "응답 데이터(도메인별 DTO)") T data
) {

  @Schema(name = "ApiResponseMetadata", description = "공통 응답 메타데이터")
  public record Metadata(
      @Schema(description = "결과", example = "SUCCESS") Result result,
      @Schema(description = "오류 코드", example = "BAD_REQUEST", nullable = true) String errorCode,
      @Schema(description = "오류/안내 메시지", example = "필드 'title' : 값은 필수입니다.", nullable = true) String message
  ) {
    @Schema(name = "ApiResponseResult", description = "응답 결과")
    public enum Result {
      SUCCESS, FAIL
    }

    public static Metadata success() {
      return new Metadata(Result.SUCCESS, null, null);
    }

    public static Metadata fail(String errorCode, String errorMessage) {
      return new Metadata(Result.FAIL, errorCode, errorMessage);
    }
  }

  public static ApiResponse<Object> success() {
    return new ApiResponse<>(Metadata.success(), null);
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(Metadata.success(), data);
  }

  public static ApiResponse<Object> fail(String errorCode, String errorMessage) {
    return new ApiResponse<>(
        Metadata.fail(errorCode, errorMessage),
        null
    );
  }
}
