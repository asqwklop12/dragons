package com.dragons.masking;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "masking")
public class MaskingProperties {
  private final Map<String, MaskingRule> rules = new HashMap<>();

  public record MaskingRule(Match match, Mask mask) {
  }

  public record Match(MatchType type, String pattern) {
  }

  public record Mask(MaskType type, String value) {
  }

  enum MatchType {
    REGEX,
    CONTAINS
  }

  enum MaskType {
    FIXED,
    PARTIAL,
    HASH
  }
}
