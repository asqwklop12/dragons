package com.dragons.executor;

import com.dragons.exception.NonRetryableException;
import com.dragons.exception.RetryableException;
import java.util.function.Supplier;

public class RetryExecutor {

  private final int maxAttempts = 3;
  private final long backoffMillis = 100;

  public <T> T execute(Supplier<T> action) {
    int attempt = 0;

    while (true) {
      try {
        return action.get();   // ← 여기 다시 실행됨
      } catch (RetryableException e) {
        attempt++;
        if (attempt >= maxAttempts) {
          throw e;           // 더 이상 못 버팀
        }
        sleep(backoffMillis);
      } catch (NonRetryableException e) {
        throw e;               // 즉시 종료
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
