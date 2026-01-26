package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class DefaultRetryPolicy implements RetryPolicy {

  private final RetryProperties.RetryProperty property;

  public DefaultRetryPolicy(RetryProperties.RetryProperty property) {
    if (property == null) {
      throw new IllegalArgumentException("RetryProperty must not be null");
    }
    this.property = property;
  }

  @Override
  public boolean retryable(Throwable e) {
    Throwable t = e;
    Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
    while (t != null && seen.add(t)) {

      if (t instanceof ResourceAccessException) {
        return true; // timeout, connection issue
      }
      if (t instanceof HttpStatusCodeException httpEx) {
        return httpEx.getStatusCode().is5xxServerError();
      }
      t = t.getCause();
    }
    return false; // 나머지는 기본적으로 재시도 가치 없음
  }

  @Override
  public int maxAttempts() {
    return property.maxAttempts();
  }

  @Override
  public long backoffMillis() {
    return property.backoffMillis();
  }
}
