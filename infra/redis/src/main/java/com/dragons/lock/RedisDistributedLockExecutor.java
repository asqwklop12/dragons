package com.dragons.lock;

import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    try {
      String token = UUID.randomUUID().toString();
      Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, options.lockAtMostFor());

      if (!Boolean.TRUE.equals(acquired)) {
        return Optional.empty();
      }

      try {
        return Optional.ofNullable(task.get());
      } finally {
        redisTemplate.delete(key);
      }

      // 레디스가 동작하지 않는 경우 shedlock 이용
    } catch (RedisSystemException e) {
      log.warn("Redis unavailable, falling back to ShedLock for key: {}", key);
      DistributedLockFactory factory = factoryProvider.getObject();
      return factory.get(LockType.SHEDLOCK).executeWithLock(key, options, task);
    }

  }

  @Override
  public LockType type() {
    return LockType.REDIS;
  }

}
