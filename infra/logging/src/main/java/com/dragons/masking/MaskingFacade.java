package com.dragons.masking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaskingFacade {
  private final MaskingPluginRegistry registry;

  public String mask(String rawBody, MaskingContext ctx) {
    return registry.applyAll(rawBody, ctx);
  }
}
