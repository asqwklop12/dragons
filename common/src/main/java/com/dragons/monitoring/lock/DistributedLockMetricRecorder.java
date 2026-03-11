package com.dragons.monitoring.lock;

import java.time.Duration;

public interface DistributedLockMetricRecorder {

  void recordAcquireSuccess(String lockType, String key, Duration elapsed);

  void recordAcquireConflict(String lockType, String key, Duration elapsed);

  void recordAcquireFallback(String lockType, String key, Duration elapsed);

  void recordTaskSuccess(String lockType, String key, Duration elapsed);

  void recordTaskFailure(String lockType, String key, Duration elapsed);

  void recordReleaseSuccess(String lockType, String key);

  void recordReleaseFailure(String lockType, String key);
}
