package com.dragons.exception;

public class NonRetryableException extends RuntimeException {
  public NonRetryableException(Throwable cause) {
    super(cause);
  }

  public NonRetryableException(String message) {
    super(message);
  }
}
