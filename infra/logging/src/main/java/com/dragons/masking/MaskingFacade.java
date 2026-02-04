package com.dragons.masking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaskingFacade {
  private final MaskingPluginRepository maskingPluginRepository;

  public String mask(String rowBody, MaskingContext ctx) {
    return maskingPluginRepository.applyAll(rowBody, ctx);
  }
}
