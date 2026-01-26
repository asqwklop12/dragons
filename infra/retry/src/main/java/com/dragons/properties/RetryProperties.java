package com.dragons.properties;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retry")
public record RetryProperties(Map<String, RetryProperty> policies) {
  public RetryProperties {
    policies = policies != null ? Map.copyOf(policies) : Map.of();
  }

  public record RetryProperty(int maxAttempts, long backoffMillis) {}
}
