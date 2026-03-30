package com.dragons.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
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

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    String token = UUID.randomUUID().toString();

    try {
      return Optional.ofNullable(task.get());
    } finally {
      try {
        redisTemplate.execute(
            new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class),
            Collections.singletonList(key),
            token
        );
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
