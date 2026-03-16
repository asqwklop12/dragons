package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.domain.lock.LockOptions;
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

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate);

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
    AtomicInteger taskRunCount = new AtomicInteger();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

    RedisDistributedLockExecutor executor = new RedisDistributedLockExecutor(redisTemplate);

    boolean acquired = executor.runWithLock(
        "lock:subscribe:test@example.com",
        LockOptions.of(Duration.ofSeconds(5)),
        taskRunCount::incrementAndGet
    );

    assertThat(acquired).isFalse();
    assertThat(taskRunCount).hasValue(0);
    verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), any(), any());
  }
}
