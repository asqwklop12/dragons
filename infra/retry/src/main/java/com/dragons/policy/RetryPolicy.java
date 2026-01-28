package com.dragons.policy;

import com.dragons.properties.RetryProperties;

public abstract class RetryPolicy {

  private final RetryProperties.RetryProperty property;

  RetryPolicy(RetryProperties.RetryProperty property) {
    if (property == null) {
      throw new IllegalArgumentException("RetryProperty must not be null");
    }
    this.property = property;
  }

  public abstract boolean retryable(Throwable e);

  public int maxAttempts() {
    return property.maxAttempts();
  }

  public long backoffMillis() {
    return property.backoffMillis();
  }
}
