package com.dragons.lock;

import com.dragons.domain.distribute.DistributedLockExecutor;
import com.dragons.domain.distribute.LockOptions;
import com.dragons.domain.distribute.LockType;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ShedLockDistributedExecutor implements DistributedLockExecutor {
  private final LockProvider lockProvider;

  @Override
  public <T> Optional<T> executeWithLock(String key, LockOptions options, Supplier<T> task) {
    LockConfiguration config = new LockConfiguration(
        Instant.now(),
        key,
        options.lockAtMostFor(),               // lockAtMostFor
        options.lockAtLeastFor()      // lockAtLeastFor
    );

    Optional<SimpleLock> lock = lockProvider.lock(config);

    if (lock.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(task.get());
    } finally {
      lock.get().unlock();
    }

  }

  @Override
  public LockType type() {
    return LockType.SHEDLOCK;
  }
}
