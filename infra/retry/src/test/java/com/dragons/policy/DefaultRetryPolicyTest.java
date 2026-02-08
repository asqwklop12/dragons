package com.dragons.policy;

import com.dragons.properties.RetryProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;

class DefaultRetryPolicyTest {

  private DefaultRetryPolicy retryPolicy;

  @BeforeEach
  void setUp() {
    RetryProperties.RetryProperty property =
        new RetryProperties.RetryProperty(3, 100L);
    retryPolicy = new DefaultRetryPolicy(property);
  }

  @Test
  void ResourceAccessException은_재시도_가능() {
    // given
    Exception exception = new ResourceAccessException("Connection timeout");

    // when
    boolean result = retryPolicy.retryable(exception);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void _5xx_에러는_재시도_가능() {
    // given
    Exception exception = new HttpServerErrorException(
        HttpStatus.INTERNAL_SERVER_ERROR, "Server error");

    // when
    boolean result = retryPolicy.retryable(exception);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void _4xx_에러는_재시도_불가능() {
    // given
    Exception exception = new HttpClientErrorException(
        HttpStatus.BAD_REQUEST, "Client error");

    // when
    boolean result = retryPolicy.retryable(exception);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void 일반_예외는_재시도_불가능() {
    // given
    Exception exception = new IllegalArgumentException("Invalid argument");

    // when
    boolean result = retryPolicy.retryable(exception);

    // then
    assertThat(result).isFalse();
  }

  @Test
  void 중첩된_예외도_올바르게_판단() {
    // given
    ResourceAccessException cause = new ResourceAccessException("Timeout");
    RuntimeException wrapper = new RuntimeException("Wrapper", cause);

    // when
    boolean result = retryPolicy.retryable(wrapper);

    // then
    assertThat(result).isTrue();
  }

  @Test
  void maxAttempts와_backoffMillis_반환() {
    // when & then
    assertThat(retryPolicy.maxAttempts()).isEqualTo(3);
    assertThat(retryPolicy.backoffMillis()).isEqualTo(100L);
  }
}
