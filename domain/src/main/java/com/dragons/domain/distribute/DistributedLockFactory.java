package com.dragons.domain.distribute;

public interface DistributedLockFactory {
  DistributedLockExecutor get(LockType type);
}
