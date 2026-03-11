package com.dragons.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.constant.Constants.Metric.DistributedLock;
import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import com.dragons.monitoring.lock.DistributedLockMetricRecorder;
import java.time.Duration;
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
  private final DistributedLockMetricRecorder metricRecorder;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    long acquireStartedAt = System.nanoTime();
    Boolean acquired;
    try {
      acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());
    } catch (RedisSystemException | QueryTimeoutException e) {
      metricRecorder.recordAcquireFallback(
          DistributedLock.LOCK_TYPE_REDIS,
          key,
          elapsedSince(acquireStartedAt)
      );
      log.warn("Redis unavailable, falling back to ShedLock for key: {}", key);
      DistributedLockFactory factory = factoryProvider.getIfAvailable();
      if (factory == null) {
        throw new LockException("Redis unavailable, falling back to ShedLock for key: " + key, e);
      }
      return factory.get(LockType.SHEDLOCK).executeWithLock(key, options, task);
    }

    if (!Boolean.TRUE.equals(acquired)) {
      metricRecorder.recordAcquireConflict(
          DistributedLock.LOCK_TYPE_REDIS,
          key,
          elapsedSince(acquireStartedAt)
      );
      return Optional.empty();
    }

    metricRecorder.recordAcquireSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        key,
        elapsedSince(acquireStartedAt)
    );
    long taskStartedAt = System.nanoTime();

    try {
      T result = task.get();
      metricRecorder.recordTaskSuccess(
          DistributedLock.LOCK_TYPE_REDIS,
          key,
          elapsedSince(taskStartedAt)
      );
      return Optional.ofNullable(result);
    } catch (RuntimeException | Error e) {
      metricRecorder.recordTaskFailure(
          DistributedLock.LOCK_TYPE_REDIS,
          key,
          elapsedSince(taskStartedAt)
      );
      throw e;
    } finally {
      try {
        redisTemplate.execute(
            new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class),
            Collections.singletonList(key),
            token
        );
        metricRecorder.recordReleaseSuccess(DistributedLock.LOCK_TYPE_REDIS, key);
      } catch (Exception e) {
        // 태스크가 이미 완료된 후이므로 재실행하지 않고 TTL 만료에 위임
        metricRecorder.recordReleaseFailure(DistributedLock.LOCK_TYPE_REDIS, key);
        log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
      }
    }
  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }

  private Duration elapsedSince(long startedAt) {
    return Duration.ofNanos(System.nanoTime() - startedAt);
  }
}
