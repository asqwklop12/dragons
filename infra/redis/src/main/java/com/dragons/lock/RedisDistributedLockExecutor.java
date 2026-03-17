package com.dragons.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class RedisDistributedLockExecutor implements DistributedLockExecutor {
  private final StringRedisTemplate redisTemplate;
  private final Counter acquireAttempts;
  private final Counter acquireFailures;
  private final Counter acquireSuccess;
  private final Counter releaseSuccess;
  private final Counter releaseFailures;
  private final Timer executionTimer;

  RedisDistributedLockExecutor(StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
    this.redisTemplate = redisTemplate;
    this.acquireAttempts = Counter.builder("redis.lock.acquire.attempts")
        .description("Redis 분산 락 획득 시도 횟수")
        .register(meterRegistry);
    this.acquireFailures = Counter.builder("redis.lock.acquire.failures")
        .description("Redis 분산 락 획득 실패 횟수 (경합)")
        .register(meterRegistry);
    this.acquireSuccess = Counter.builder("redis.lock.acquire.success")
        .description("Redis 분산 락 획득 성공 횟수")
        .register(meterRegistry);
    this.releaseSuccess = Counter.builder("redis.lock.release.success")
        .description("Redis 분산 락 정상 해제 횟수")
        .register(meterRegistry);
    this.releaseFailures = Counter.builder("redis.lock.release.failures")
        .description("Redis 분산 락 해제 실패 횟수")
        .register(meterRegistry);
    this.executionTimer = Timer.builder("redis.lock.execution.duration")
        .description("Redis 분산 락 보유 중 작업 실행 시간")
        .register(meterRegistry);
  }

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    acquireAttempts.increment();

    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());

    if (!Boolean.TRUE.equals(acquired)) {
      acquireFailures.increment();
      return Optional.empty();
    }

    // 락 점유 시작
    Timer.Sample sample = Timer.start();
    try {
      acquireSuccess.increment();
      return Optional.ofNullable(task.get());
    } finally {
      try {
        Long released = redisTemplate.execute(
            new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class),
            Collections.singletonList(key),
            token
        );

        // 해제 실패
        if (!Long.valueOf(1L).equals(released)) {
          releaseFailures.increment();
          log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);

       // 해제 성공
        } else {
          releaseSuccess.increment();
        }
      // 해제 실패
      } catch (Exception exception) {
        releaseFailures.increment();
        log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
      } finally {
        // 시간 획득 종료
        sample.stop(executionTimer);  // task + release 전체 락 점유 시간
      }
    }
  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }
}
