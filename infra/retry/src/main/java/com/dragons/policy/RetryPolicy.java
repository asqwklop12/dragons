package com.dragons.policy;

public interface RetryPolicy {
  boolean retryable(Throwable e);
}
