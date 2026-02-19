package com.dragons.domain.lock;

public interface DistributedLockFactory {
  DistributedLockExecutor get(LockType type);
}
