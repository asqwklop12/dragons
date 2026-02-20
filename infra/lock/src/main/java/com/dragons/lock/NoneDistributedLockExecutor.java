package com.dragons.lock;

import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class NoneDistributedLockExecutor implements DistributedLockExecutor {
  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
     return Optional.ofNullable(task.get());
  }

  @Override
  public LockType type() {
    return LockType.NONE;
  }
}
