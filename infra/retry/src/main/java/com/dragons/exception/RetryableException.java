package com.dragons.exception;

public class RetryableException extends RuntimeException {
  public RetryableException(Throwable cause) {
    super(cause);
  }

  public RetryableException(String message) {
    super(message);
  }

  public RetryableException(String message, Throwable cause) {
    super(message, cause);
  }
}
