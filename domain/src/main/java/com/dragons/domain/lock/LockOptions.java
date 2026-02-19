package com.dragons.domain.lock;

import java.time.Duration;

public record LockOptions(Duration lockAtMostFor, Duration lockAtLeastFor) {

  public LockOptions {
    if (lockAtMostFor == null || lockAtMostFor.isZero() || lockAtMostFor.isNegative()) {
      throw new LockException("lockAtMostFor must be a positive duration");
    }
    if (lockAtLeastFor == null || lockAtLeastFor.isNegative()) {
      throw new LockException("lockAtLeastFor must be non-negative");
    }
    if (lockAtLeastFor.compareTo(lockAtMostFor) > 0) {
      throw new LockException("lockAtLeastFor must not exceed lockAtMostFor");
    }
  }


  public static LockOptions of(Duration ttl) {
    return new LockOptions(ttl, Duration.ZERO);
  }

  public static LockOptions of(Duration lockAtMostFor, Duration lockAtLeastFor) {
    return new LockOptions(lockAtMostFor, lockAtLeastFor);
  }
}
