package com.dragons.masking;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaskingPluginRegistry {
  private final List<MaskingPlugin> plugins;

  public String applyAll(String body, MaskingContext ctx) {
    if (body == null || body.isBlank()) return body;

    return plugins.stream()
        .filter(p -> p.supports(ctx))
        .sorted(Comparator
            .comparing(MaskingPlugin::phase)
            .thenComparingInt(MaskingPlugin::order))
        .reduce(body,
            (acc, plugin) -> plugin.apply(acc, ctx),
            (a, b) -> b
        );
  }
}
