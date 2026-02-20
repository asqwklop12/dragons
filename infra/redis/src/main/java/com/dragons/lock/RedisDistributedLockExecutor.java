package com.dragons.lock;

import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class RedisDistributedLockExecutor implements DistributedLockExecutor {
  private final StringRedisTemplate redisTemplate;
  private final ObjectProvider<DistributedLockFactory> factoryProvider;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    Boolean acquired;
    try {
      acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());
    } catch (RedisSystemException | QueryTimeoutException e) {
      log.warn("Redis unavailable, falling back to ShedLock for key: {}", key);
      DistributedLockFactory factory = factoryProvider.getIfAvailable();
      if (factory == null) {
        throw new LockException("Redis unavailable, falling back to ShedLock for key: " + key, e);
      }
      return factory.get(LockType.SHEDLOCK).executeWithLock(key, options, task);
    }

    if (!Boolean.TRUE.equals(acquired)) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(task.get());
    } finally {
      try {
        redisTemplate.delete(key);
      } catch (Exception e) {
        // 태스크가 이미 완료된 후이므로 재실행하지 않고 TTL 만료에 위임
        log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
      }
    }
  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }

}
