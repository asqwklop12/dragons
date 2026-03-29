package com.dragons.domain.lock;

import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockExecutor {
  <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task);

  default boolean runWithLock(String key, LockOptions options, Runnable task) {
    return executeWithLock(key, options, () -> {
      task.run();
      return Boolean.TRUE;
    }).isPresent();
  }

  LockType type();
}
