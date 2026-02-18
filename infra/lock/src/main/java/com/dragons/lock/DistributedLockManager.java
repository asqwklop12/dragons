package com.dragons.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface DistributedLockManager {
  <T> Optional<T> executeWithLock(String key,
                                  Duration ttl,
                                  Supplier<T> task);

  boolean executeWithLock(String key, Duration ttl, Runnable task);
}
