package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dragons.constant.Constants.Metric.DistributedLock;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.monitoring.lock.DistributedLockMetricRecorder;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

class RedisDistributedLockExecutorTest {

  @Test
  @DisplayName("Redis 락 획득에 성공하면 메트릭을 기록한다")
  void shouldRecordMetricEventsWhenLockAcquired() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ObjectProvider<DistributedLockFactory> factoryProvider = mock(ObjectProvider.class);
    DistributedLockMetricRecorder metricRecorder = mock(DistributedLockMetricRecorder.class);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
    when(
        redisTemplate.execute(
            any(DefaultRedisScript.class),
            eq(Collections.singletonList("lock:payment-confirm:order-1")),
            any()
        )
    )
        .thenReturn(1L);

    RedisDistributedLockExecutor executor =
        new RedisDistributedLockExecutor(redisTemplate, factoryProvider, metricRecorder);

    Optional<String> result = executor.executeWithLock(
        "lock:payment-confirm:order-1",
        LockOptions.of(Duration.ofSeconds(10)),
        () -> "ok"
    );

    assertThat(result).contains("ok");
    verify(metricRecorder).recordAcquireSuccess(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq("lock:payment-confirm:order-1"),
        any(Duration.class)
    );
    verify(metricRecorder).recordTaskSuccess(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq("lock:payment-confirm:order-1"),
        any(Duration.class)
    );
    verify(metricRecorder).recordReleaseSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        "lock:payment-confirm:order-1"
    );
    verify(metricRecorder, never()).recordAcquireConflict(any(), any(), any());
  }

  @Test
  void shouldRecordOnlyConflictMetricEventWhenLockAcquireFails() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ObjectProvider<DistributedLockFactory> factoryProvider = mock(ObjectProvider.class);
    DistributedLockMetricRecorder metricRecorder = mock(DistributedLockMetricRecorder.class);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

    RedisDistributedLockExecutor executor =
        new RedisDistributedLockExecutor(redisTemplate, factoryProvider, metricRecorder);

    Optional<String> result = executor.executeWithLock(
        "lock:subscribe:test@example.com",
        LockOptions.of(Duration.ofSeconds(5)),
        () -> "should-not-run"
    );

    assertThat(result).isEmpty();
    verify(metricRecorder).recordAcquireConflict(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq("lock:subscribe:test@example.com"),
        any(Duration.class)
    );
    verify(metricRecorder, never()).recordTaskSuccess(any(), any(), any());
    verify(metricRecorder, never()).recordReleaseSuccess(any(), any());
    verifyNoInteractions(factoryProvider);
  }
}
