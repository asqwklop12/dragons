package com.dragons.monitoring.lock;

import com.dragons.constant.Constants.Lock;
import com.dragons.constant.Constants.Metric.DistributedLock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerDistributedLockMetricRecorder implements DistributedLockMetricRecorder {
  private final MeterRegistry meterRegistry;

  @Override
  public void recordAcquireSuccess(String lockType, String key, Duration elapsed) {
    recordAcquire(lockType, key, DistributedLock.OUTCOME_SUCCESS, elapsed);
  }

  @Override
  public void recordAcquireConflict(String lockType, String key, Duration elapsed) {
    recordAcquire(lockType, key, DistributedLock.OUTCOME_CONFLICT, elapsed);
  }

  @Override
  public void recordAcquireFallback(String lockType, String key, Duration elapsed) {
    recordAcquire(lockType, key, DistributedLock.OUTCOME_FALLBACK, elapsed);
  }

  @Override
  public void recordTaskSuccess(String lockType, String key, Duration elapsed) {
    recordTask(lockType, key, DistributedLock.OUTCOME_SUCCESS, elapsed);
  }

  @Override
  public void recordTaskFailure(String lockType, String key, Duration elapsed) {
    recordTask(lockType, key, DistributedLock.OUTCOME_FAILURE, elapsed);
  }

  @Override
  public void recordReleaseSuccess(String lockType, String key) {
    recordRelease(lockType, key, DistributedLock.OUTCOME_SUCCESS);
  }

  @Override
  public void recordReleaseFailure(String lockType, String key) {
    recordRelease(lockType, key, DistributedLock.OUTCOME_FAILURE);
  }

  private void recordAcquire(String lockType, String key, String outcome, Duration elapsed) {
    String lockName = resolveLockName(key);
    meterRegistry.counter(
        DistributedLock.ACQUIRE,
        DistributedLock.TAG_LOCK_TYPE, lockType,
        DistributedLock.TAG_LOCK_NAME, lockName,
        DistributedLock.TAG_OUTCOME, outcome
    ).increment();
    Timer.builder(DistributedLock.ACQUIRE_TIME)
        .tag(DistributedLock.TAG_LOCK_TYPE, lockType)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .register(meterRegistry)
        .record(elapsed);
  }

  private void recordTask(String lockType, String key, String outcome, Duration elapsed) {
    Timer.builder(DistributedLock.TASK)
        .tag(DistributedLock.TAG_LOCK_TYPE, lockType)
        .tag(DistributedLock.TAG_LOCK_NAME, resolveLockName(key))
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .register(meterRegistry)
        .record(elapsed);
  }

  private void recordRelease(String lockType, String key, String outcome) {
    meterRegistry.counter(
        DistributedLock.RELEASE,
        DistributedLock.TAG_LOCK_TYPE, lockType,
        DistributedLock.TAG_LOCK_NAME, resolveLockName(key),
        DistributedLock.TAG_OUTCOME, outcome
    ).increment();
  }

  private String resolveLockName(String key) {
    if (key.startsWith(Lock.LOCK_PAYMENT_CONFIRM)) {
      return DistributedLock.LOCK_NAME_PAYMENT_CONFIRM;
    }
    if (key.startsWith(Lock.LOCK_SUBSCRIBE)) {
      return DistributedLock.LOCK_NAME_SUBSCRIBE;
    }
    if (Lock.LOCK_BANK_DEPOSIT.equals(key)) {
      return DistributedLock.LOCK_NAME_BANK_DEPOSIT;
    }
    if (Lock.LOCK_EXPIRED_SUBSCRIPTION.equals(key)) {
      return DistributedLock.LOCK_NAME_EXPIRED_SUBSCRIPTION;
    }
    return DistributedLock.LOCK_NAME_UNKNOWN;
  }
}
