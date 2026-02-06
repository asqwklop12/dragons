package com.dragons.masking;

public record MaskingContext(
    RequestOrigin origin,
    String path
) {
  // Client: 외부 API 사용시
  // System: 내부 API 사용시
  public enum RequestOrigin {CLIENT, SYSTEM}
}
