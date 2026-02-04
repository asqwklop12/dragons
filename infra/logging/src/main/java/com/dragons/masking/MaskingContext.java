package com.dragons.masking;

public record MaskingContext(
    RequestOrigin origin,
    String path
) {
  public enum RequestOrigin {CLIENT, SYSTEM}
}
