package com.dragons.policy;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class DefaultRetryPolicy implements RetryPolicy {
  @Override
  public boolean retryable(Throwable e) {
    if (e instanceof ResourceAccessException) {
      return true; // timeout, connection issue
    }
    if (e instanceof HttpStatusCodeException httpEx) {
      return httpEx.getStatusCode().is5xxServerError();
    }
    return false; // 나머지는 기본적으로 재시도 가치 없음
  }
}
