package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class SocialLoginRetryPolicy extends RetryPolicy {

  public SocialLoginRetryPolicy(RetryProperties.RetryProperty property) {
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
        if (httpEx.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
          return true;
        }
        return httpEx.getStatusCode().is5xxServerError();
      }
      t = t.getCause();
    }
    return false;
  }
}
