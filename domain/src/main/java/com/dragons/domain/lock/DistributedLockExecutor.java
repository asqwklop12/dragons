package com.dragons.domain.lock;

import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockExecutor {
  <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task);

  LockType type();
}
