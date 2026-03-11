package com.dragons.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.constant.Constants.Metric.DistributedLock;
import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class RedisDistributedLockExecutor implements DistributedLockExecutor {
  private final StringRedisTemplate redisTemplate;
  private final ObjectProvider<DistributedLockFactory> factoryProvider;
  private final MeterRegistry meterRegistry;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    String lockName = resolveLockName(key);
    Timer.Sample acquireSample = Timer.start(meterRegistry);
    Boolean acquired;
    try {
      acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());
    } catch (RedisSystemException | QueryTimeoutException e) {
      incrementAcquire(lockName, DistributedLock.OUTCOME_FALLBACK);
      acquireSample.stop(acquireTimer(lockName, DistributedLock.OUTCOME_FALLBACK));
      log.warn("Redis unavailable, falling back to ShedLock for key: {}", key);
      DistributedLockFactory factory = factoryProvider.getIfAvailable();
      if (factory == null) {
        throw new LockException("Redis unavailable, falling back to ShedLock for key: " + key, e);
      }
      return factory.get(LockType.SHEDLOCK).executeWithLock(key, options, task);
    }

    if (!Boolean.TRUE.equals(acquired)) {
      incrementAcquire(lockName, DistributedLock.OUTCOME_CONFLICT);
      acquireSample.stop(acquireTimer(lockName, DistributedLock.OUTCOME_CONFLICT));
      return Optional.empty();
    }

    incrementAcquire(lockName, DistributedLock.OUTCOME_SUCCESS);
    acquireSample.stop(acquireTimer(lockName, DistributedLock.OUTCOME_SUCCESS));
    Timer.Sample sample = Timer.start(meterRegistry);

    try {
      T result = task.get();
      sample.stop(taskTimer(lockName, DistributedLock.OUTCOME_SUCCESS));
      return Optional.ofNullable(result);
    } catch (RuntimeException | Error e) {
      sample.stop(taskTimer(lockName, DistributedLock.OUTCOME_FAILURE));
      throw e;
    } finally {
      try {
        redisTemplate.execute(
            new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class),
            Collections.singletonList(key),
            token
        );
        meterRegistry.counter(
            DistributedLock.RELEASE,
            DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS,
            DistributedLock.TAG_LOCK_NAME, lockName,
            DistributedLock.TAG_OUTCOME, DistributedLock.OUTCOME_SUCCESS
        ).increment();
      } catch (Exception e) {
        // 태스크가 이미 완료된 후이므로 재실행하지 않고 TTL 만료에 위임
        meterRegistry.counter(
            DistributedLock.RELEASE,
            DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS,
            DistributedLock.TAG_LOCK_NAME, lockName,
            DistributedLock.TAG_OUTCOME, DistributedLock.OUTCOME_FAILURE
        ).increment();
        log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
      }
    }
  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }

  private void incrementAcquire(String lockName, String outcome) {
    meterRegistry.counter(
        DistributedLock.ACQUIRE,
        DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS,
        DistributedLock.TAG_LOCK_NAME, lockName,
        DistributedLock.TAG_OUTCOME, outcome
    ).increment();
  }

  private Timer taskTimer(String lockName, String outcome) {
    return Timer.builder(DistributedLock.TASK)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .register(meterRegistry);
  }

  private Timer acquireTimer(String lockName, String outcome) {
    return Timer.builder(DistributedLock.ACQUIRE_TIME)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .register(meterRegistry);
  }

  private String resolveLockName(String key) {
    if (key.startsWith(Lock.LOCK_PAYMENT_CONFIRM)) {
      return DistributedLock.LOCK_NAME_PAYMENT_CONFIRM;
    }
    if (key.startsWith(Lock.LOCK_SUBSCRIBE)) {
      return DistributedLock.LOCK_NAME_SUBSCRIBE;
    }
    if (Lock.LOCK_BANK_DEPOSIT.equals(key)) {
      return DistributedLock.LOCK_NAME_BANK_DEPOSIT;
    }
    if (Lock.LOCK_EXPIRED_SUBSCRIPTION.equals(key)) {
      return DistributedLock.LOCK_NAME_EXPIRED_SUBSCRIPTION;
    }
    return DistributedLock.LOCK_NAME_UNKNOWN;
  }
}
