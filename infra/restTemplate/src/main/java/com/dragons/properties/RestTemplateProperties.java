package com.dragons.properties;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rest-template")
public record RestTemplateProperties(Map<String, RestTemplateProperty> policies) {
  public RestTemplateProperties {
    policies = policies != null ? Map.copyOf(policies) : Map.of();
  }

  public record RestTemplateProperty(int connectTimeout, long readTimeout) {}
}
