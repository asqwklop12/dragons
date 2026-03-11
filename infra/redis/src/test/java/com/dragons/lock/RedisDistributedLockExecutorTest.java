package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dragons.constant.Constants.Metric.DistributedLock;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisDistributedLockExecutorTest {

  @Test
  void shouldRecordMetricsWhenLockAcquired() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ObjectProvider<DistributedLockFactory> factoryProvider = mock(ObjectProvider.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

    RedisDistributedLockExecutor executor =
        new RedisDistributedLockExecutor(redisTemplate, factoryProvider, meterRegistry);

    Optional<String> result = executor.executeWithLock(
        "lock:payment-confirm:order-1",
        LockOptions.of(Duration.ofSeconds(10)),
        () -> "ok"
    );

    assertThat(result).contains("ok");
    assertThat(counterValue(meterRegistry, DistributedLock.ACQUIRE,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM, DistributedLock.OUTCOME_SUCCESS))
        .isEqualTo(1.0);
    assertThat(timerCount(meterRegistry, DistributedLock.ACQUIRE_TIME,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM, DistributedLock.OUTCOME_SUCCESS))
        .isEqualTo(1);
    assertThat(counterValue(meterRegistry, DistributedLock.RELEASE,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM, DistributedLock.OUTCOME_SUCCESS))
        .isEqualTo(1.0);
    assertThat(meterRegistry.get(DistributedLock.TASK)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, DistributedLock.LOCK_NAME_PAYMENT_CONFIRM)
        .tag(DistributedLock.TAG_OUTCOME, DistributedLock.OUTCOME_SUCCESS)
        .timer()
        .count()).isEqualTo(1);
  }

  @Test
  void shouldRecordOnlyConflictMetricWhenLockAcquireFails() {
    StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
    @SuppressWarnings("unchecked")
    ObjectProvider<DistributedLockFactory> factoryProvider = mock(ObjectProvider.class);
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

    RedisDistributedLockExecutor executor =
        new RedisDistributedLockExecutor(redisTemplate, factoryProvider, meterRegistry);

    Optional<String> result = executor.executeWithLock(
        "lock:subscribe:test@example.com",
        LockOptions.of(Duration.ofSeconds(5)),
        () -> "should-not-run"
    );

    assertThat(result).isEmpty();
    assertThat(counterValue(meterRegistry, DistributedLock.ACQUIRE,
        DistributedLock.LOCK_NAME_SUBSCRIBE, DistributedLock.OUTCOME_CONFLICT))
        .isEqualTo(1.0);
    assertThat(timerCount(meterRegistry, DistributedLock.ACQUIRE_TIME,
        DistributedLock.LOCK_NAME_SUBSCRIBE, DistributedLock.OUTCOME_CONFLICT))
        .isEqualTo(1);
    assertThat(meterRegistry.find(DistributedLock.RELEASE)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, DistributedLock.LOCK_NAME_SUBSCRIBE)
        .tag(DistributedLock.TAG_OUTCOME, DistributedLock.OUTCOME_SUCCESS)
        .counter()).isNull();
    verifyNoInteractions(factoryProvider);
  }

  private double counterValue(
      SimpleMeterRegistry meterRegistry,
      String metricName,
      String lockName,
      String outcome
  ) {
    return meterRegistry.get(metricName)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .counter()
        .count();
  }

  private long timerCount(
      SimpleMeterRegistry meterRegistry,
      String metricName,
      String lockName,
      String outcome
  ) {
    return meterRegistry.get(metricName)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .timer()
        .count();
  }
}
