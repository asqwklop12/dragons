package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class SocialLoginRetryPolicy implements RetryPolicy {
  private final RetryProperties.RetryProperty property;

  public SocialLoginRetryPolicy(RetryProperties.RetryProperty property) {
    if (property == null) {
      throw new IllegalArgumentException("RetryProperty must not be null");
    }
    this.property = property;
  }

  @Override
  public boolean retryable(Throwable e) {
    Throwable t = e;
    while (t != null) {

      if (t instanceof ResourceAccessException) {
        return true; // timeout, connection issue
      }
      if (t instanceof HttpStatusCodeException httpEx) {
        if (httpEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
          return true;
        }
        return httpEx.getStatusCode().is5xxServerError();
      }
      t = t.getCause();
    }
    return false;
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
