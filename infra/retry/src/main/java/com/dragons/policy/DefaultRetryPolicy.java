package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class DefaultRetryPolicy extends RetryPolicy {

  public DefaultRetryPolicy(RetryProperties.RetryProperty property) {
    super(property);
  }

  @Override
  public boolean retryable(Throwable e) {
    Throwable t = e;
    while (t != null) {

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

}
