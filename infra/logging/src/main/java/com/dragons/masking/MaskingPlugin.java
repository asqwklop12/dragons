package com.dragons.masking;

public interface MaskingPlugin {
  String apply(String body);
  boolean supports(MaskingContext ctx);
}
