package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.domain.lock.LockOptions;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

class RedisDistributedLockExecutorTest {

  @Test
  void shouldExecuteTaskAndReleaseLockWhenAcquired() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
    when(
        redisTemplate.execute(
            any(DefaultRedisScript.class),
            eq(Collections.singletonList("lock:payment-confirm:order-1")),
            any()
        )
    ).thenReturn(1L);

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate, meterRegistry);

    Optional<String> result = executor.executeWithLock(
        "lock:payment-confirm:order-1",
        LockOptions.of(Duration.ofSeconds(10)),
        () -> {
          taskRunCount.incrementAndGet();
          return "ok";
        }
    );

    assertThat(result).contains("ok");
    assertThat(taskRunCount).hasValue(1);
    verify(redisTemplate).execute(
        any(DefaultRedisScript.class),
        eq(Collections.singletonList("lock:payment-confirm:order-1")),
        any()
    );
  }

  @Test
  void shouldReturnFalseWhenRunWithLockAcquireFails() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate, meterRegistry);

    boolean acquired = executor.runWithLock(
        "lock:subscribe:test@example.com",
        LockOptions.of(Duration.ofSeconds(5)),
        taskRunCount::incrementAndGet
    );

    assertThat(acquired).isFalse();
    assertThat(taskRunCount).hasValue(0);
    verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), any(), any());
  }

  @Test
  void shouldPropagateExceptionWhenRedisFailsDuringAcquire() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class)))
        .thenThrow(new RuntimeException("redis unavailable"));

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate, meterRegistry);

    assertThatThrownBy(() -> executor.runWithLock(
        "lock:subscribe:test@example.com",
        LockOptions.of(Duration.ofSeconds(5)),
        taskRunCount::incrementAndGet
    ))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("redis unavailable");

    assertThat(taskRunCount).hasValue(0);
    assertThat(meterRegistry.counter("redis.lock.acquire.attempts").count()).isEqualTo(1);
    assertThat(meterRegistry.counter("redis.lock.acquire.failures").count()).isEqualTo(0);
    verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), any(), any());
  }

  @Test
  void shouldReturnResultAndRecordFailureWhenReleaseReturnsZero() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
    when(
        redisTemplate.execute(
            any(DefaultRedisScript.class),
            eq(Collections.singletonList("lock:payment-confirm:order-1")),
            any()
        )
    ).thenReturn(0L);

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate, meterRegistry);

    Optional<String> result = executor.executeWithLock(
        "lock:payment-confirm:order-1",
        LockOptions.of(Duration.ofSeconds(10)),
        () -> {
          taskRunCount.incrementAndGet();
          return "ok";
        }
    );

    assertThat(result).contains("ok");
    assertThat(taskRunCount).hasValue(1);
    assertThat(meterRegistry.counter("redis.lock.acquire.success").count()).isEqualTo(1);
    assertThat(meterRegistry.counter("redis.lock.release.success").count()).isEqualTo(0);
    assertThat(meterRegistry.counter("redis.lock.release.failures").count()).isEqualTo(1);
    assertThat(meterRegistry.timer("redis.lock.execution.duration").count()).isEqualTo(1);
  }

  @Test
  void shouldReturnResultAndRecordFailureWhenReleaseThrowsException() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
    when(
        redisTemplate.execute(
            any(DefaultRedisScript.class),
            eq(Collections.singletonList("lock:payment-confirm:order-1")),
            any()
        )
    ).thenThrow(new RuntimeException("release failed"));

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate, meterRegistry);

    Optional<String> result = executor.executeWithLock(
        "lock:payment-confirm:order-1",
        LockOptions.of(Duration.ofSeconds(10)),
        () -> {
          taskRunCount.incrementAndGet();
          return "ok";
        }
    );

    assertThat(result).contains("ok");
    assertThat(taskRunCount).hasValue(1);
    assertThat(meterRegistry.counter("redis.lock.acquire.success").count()).isEqualTo(1);
    assertThat(meterRegistry.counter("redis.lock.release.success").count()).isEqualTo(0);
    assertThat(meterRegistry.counter("redis.lock.release.failures").count()).isEqualTo(1);
    assertThat(meterRegistry.timer("redis.lock.execution.duration").count()).isEqualTo(1);
  }
}
