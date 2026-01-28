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

  public long nextBackoffMillis(int attempt) {

    int safeShift = Math.min(attempt - 1, 62);
    long exponential = property.backoffMillis() * (1L << safeShift);

    // 상한선 적용
    long capped = Math.min(exponential, property.maxBackoffMillis());

    // 지터 적용
    if (property.jitterRatio() <= 0) {
      return capped;
    }

    long jitterBound = (long) (capped * property.jitterRatio());
    long jitter = ThreadLocalRandom.current().nextLong(0, jitterBound + 1);

    return capped - jitter;
  }


  public int maxAttempts() {
    return property.maxAttempts();
  }

  public long backoffMillis() {
    return property.backoffMillis();
  }
}
