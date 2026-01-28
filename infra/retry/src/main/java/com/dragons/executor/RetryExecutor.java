package com.dragons.executor;

import com.dragons.exception.NonRetryableException;
import com.dragons.exception.RetryableException;
import com.dragons.policy.RetryPolicy;
import java.util.function.Supplier;

public class RetryExecutor {

  private final RetryPolicy retryPolicy;

  public RetryExecutor(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
  }

  public <T> T execute(Supplier<T> action) {
    int attempt = 0;

    while (true) {
      try {
        return action.get();   // ← 여기 다시 실행됨
      } catch (Exception e) {
        if (!retryPolicy.retryable(e)) {
          throw new NonRetryableException(e);
        }
        attempt++;
        if (attempt >= retryPolicy.maxAttempts()) {
          throw new RetryableException(e);
        }
        sleep(retryPolicy.nextBackoffMillis());
      }
    }
  }

  private void sleep(final long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt(); // 인터럽트 복구
      throw new RuntimeException("Retry interrupted", ie);
    }
  }
}
