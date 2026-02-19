package com.dragons.domain.distribute;

import java.time.Duration;

public record LockOptions(Duration lockAtMostFor, Duration lockAtLeastFor) {

  public static LockOptions of(Duration ttl) {
    return new LockOptions(ttl, Duration.ZERO);
  }

  public static LockOptions of(Duration lockAtMostFor, Duration lockAtLeastFor) {
    return new LockOptions(lockAtMostFor, lockAtLeastFor);
  }
}
