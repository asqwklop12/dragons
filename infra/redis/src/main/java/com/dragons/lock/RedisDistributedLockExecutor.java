package com.dragons.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class RedisDistributedLockExecutor implements DistributedLockExecutor {
  private final StringRedisTemplate redisTemplate;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();
    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());

    if (!Boolean.TRUE.equals(acquired)) {
      return Optional.empty();
    }

    try {
      return Optional.ofNullable(task.get());
    } finally {
      try {
        Long released = redisTemplate.execute(
            new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class),
            Collections.singletonList(key),
            token
        );

        if (!Long.valueOf(1L).equals(released)) {
          log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
        }
      } catch (Exception exception) {
        log.warn("Failed to release Redis lock for key: {}, will expire by TTL", key);
      }
    }
  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }
}
