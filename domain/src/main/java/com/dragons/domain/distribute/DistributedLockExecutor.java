package com.dragons.domain.distribute;

import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockExecutor {
  <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task);

  LockType type();
}
