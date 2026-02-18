package com.dragons.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockManager implements DistributedLockManager {
  private final StringRedisTemplate redisTemplate;
  @Override
  public <T> Optional<T> executeWithLock(String key, Duration ttl, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);

    if (!acquired) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(task.get());
    } finally {
      redisTemplate.delete(key);
    }

  }

  @Override
  public boolean executeWithLock(String key, Duration ttl, Runnable task) {
    return executeWithLock(key, ttl, () -> {
      task.run();
      return true;
    }).isPresent();
  }
}
