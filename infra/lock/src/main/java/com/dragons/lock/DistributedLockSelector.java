package com.dragons.lock;

import com.dragons.domain.distribute.DistributedLockExecutor;
import com.dragons.domain.distribute.DistributedLockFactory;
import com.dragons.domain.distribute.LockType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class DistributedLockSelector implements DistributedLockFactory {
  private final Map<LockType, DistributedLockExecutor> executors;

  public DistributedLockSelector(List<DistributedLockExecutor> executors) {
    this.executors = executors.stream()
        .collect(Collectors.toUnmodifiableMap(DistributedLockExecutor::type, Function.identity(),
            (a, b) -> {
              throw new LockException("Duplicate executor registered for type: " + a.type());
            }
        ));
  }


  @Override
  public DistributedLockExecutor get(LockType type) {
    DistributedLockExecutor executor = executors.get(type);
    if (executor == null) {
      throw new LockException("No executor for type: " + type);
    }
    return executor;
  }
}
