package com.dragons.masking;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DefaultMaskingPlugin implements MaskingPlugin {
  private final MaskingEngine engine;

  @Override
  public boolean supports(MaskingContext ctx) {
    return true;
  }

  @Override
  public String apply(String body) {
    return engine.mask(body);
  }
}
