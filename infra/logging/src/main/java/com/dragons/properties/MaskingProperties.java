package com.dragons.properties;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "masking")
public record MaskingProperties(Map<String, MaskingRule> rules) {

  public MaskingProperties {
    rules = rules != null ? Map.copyOf(rules) : new HashMap<>();
  }
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
