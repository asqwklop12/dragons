package com.dragons.lock;

import com.dragons.domain.distribute.DistributedLockExecutor;
import com.dragons.domain.distribute.LockOptions;
import com.dragons.domain.distribute.LockType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RedisDistributedLockExecutor implements DistributedLockExecutor {
  private final StringRedisTemplate redisTemplate;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());

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
  public LockType type() {
    return LockType.REDIS;
  }

}
