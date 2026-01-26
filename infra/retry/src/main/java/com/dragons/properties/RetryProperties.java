package com.dragons.properties;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retry")
@Getter
public class RetryProperties {
  private final Map<String, RetryProperty> policies = new HashMap<>();


  @Setter
  @Getter
  public static final class RetryProperty {
    private int maxAttempts;
    private long backoffMillis;

  }
}
