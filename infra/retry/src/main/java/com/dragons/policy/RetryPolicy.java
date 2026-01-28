package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import java.util.concurrent.ThreadLocalRandom;

public abstract class RetryPolicy {

  private final RetryProperties.RetryProperty property;

  RetryPolicy(RetryProperties.RetryProperty property) {
    if (property == null) {
      throw new IllegalArgumentException("RetryProperty must not be null");
    }
    this.property = property;
  }

  public abstract boolean retryable(Throwable e);

  public long nextBackoffMillis() {
    long base = property.backoffMillis();
    double ratio = property.jitterRatio();

    if (ratio <= 0) {
      return base;
    }

    long bound = Math.max(1, (long) (base * ratio));
    long jitter = ThreadLocalRandom.current().nextLong(0, bound + 1);
    return base + jitter;
  }


  public int maxAttempts() {
    return property.maxAttempts();
  }

  public long backoffMillis() {
    return property.backoffMillis();
  }
}
