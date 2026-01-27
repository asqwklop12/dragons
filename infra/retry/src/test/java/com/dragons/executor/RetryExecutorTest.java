package com.dragons.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.dragons.exception.NonRetryableException;
import com.dragons.exception.RetryableException;
import com.dragons.policy.RetryPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class RetryExecutorTest {

  @Mock
  private RetryPolicy retryPolicy;

  private RetryExecutor retryExecutor;

  @BeforeEach
  void setUp() {
    retryExecutor = new RetryExecutor(retryPolicy);
  }

  @Test
  void 첫_시도에서_성공하면_즉시_반환() {
    // given
    Supplier<String> successAction = () -> "success";

    // when
    String result = retryExecutor.execute(successAction);

    // then
    assertThat(result).isEqualTo("success");
    verify(retryPolicy, never()).retryable(any());
  }

  @Test
  void 재시도_가능한_예외_발생시_재시도_후_성공() {
    // given
    when(retryPolicy.retryable(any())).thenReturn(true);
    when(retryPolicy.maxAttempts()).thenReturn(3);
    when(retryPolicy.backoffMillis()).thenReturn(10L);

    AtomicInteger attemptCount = new AtomicInteger(0);
    Supplier<String> action = () -> {
      int attempt = attemptCount.incrementAndGet();
      if (attempt < 3) {
        throw new ResourceAccessException("Connection timeout");
      }
      return "success after retry";
    };

    // when
    String result = retryExecutor.execute(action);

    // then
    assertThat(result).isEqualTo("success after retry");
    assertThat(attemptCount.get()).isEqualTo(3);
    verify(retryPolicy, times(2)).retryable(any());
  }

  @Test
  void 재시도_불가능한_예외는_즉시_NonRetryableException_발생() {
    // given
    when(retryPolicy.retryable(any())).thenReturn(false);

    Supplier<String> action = () -> {
      throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "Client error");
    };

    // when & then
    assertThatThrownBy(() -> retryExecutor.execute(action))
        .isInstanceOf(NonRetryableException.class)
        .hasCauseInstanceOf(HttpServerErrorException.class);

    verify(retryPolicy, times(1)).retryable(any());
  }

  @Test
  void 최대_재시도_횟수_초과시_RetryableException_발생() {
    // given
    when(retryPolicy.retryable(any())).thenReturn(true);
    when(retryPolicy.maxAttempts()).thenReturn(3);
    when(retryPolicy.backoffMillis()).thenReturn(10L);

    AtomicInteger attemptCount = new AtomicInteger(0);
    Supplier<String> action = () -> {
      attemptCount.incrementAndGet();
      throw new ResourceAccessException("Always fails");
    };

    // when & then
    assertThatThrownBy(() -> retryExecutor.execute(action))
        .isInstanceOf(RetryableException.class)
        .hasCauseInstanceOf(ResourceAccessException.class);

    assertThat(attemptCount.get()).isEqualTo(3);
    verify(retryPolicy, times(3)).retryable(any());
  }
}
