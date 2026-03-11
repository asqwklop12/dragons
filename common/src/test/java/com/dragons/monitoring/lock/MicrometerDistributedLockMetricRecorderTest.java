package com.dragons.monitoring.lock;

import static org.assertj.core.api.Assertions.assertThat;

import com.dragons.constant.Constants.Metric.DistributedLock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MicrometerDistributedLockMetricRecorderTest {

  @Test
  void shouldRecordAcquireTaskAndReleaseMetrics() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    MicrometerDistributedLockMetricRecorder recorder =
        new MicrometerDistributedLockMetricRecorder(meterRegistry);

    recorder.recordAcquireSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        "lock:payment-confirm:order-1",
        Duration.ofMillis(10)
    );
    recorder.recordTaskSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        "lock:payment-confirm:order-1",
        Duration.ofMillis(20)
    );
    recorder.recordReleaseSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        "lock:payment-confirm:order-1"
    );

    assertThat(counterValue(
        meterRegistry,
        DistributedLock.ACQUIRE,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM,
        DistributedLock.OUTCOME_SUCCESS
    )).isEqualTo(1.0);
    assertThat(timerCount(
        meterRegistry,
        DistributedLock.ACQUIRE_TIME,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM,
        DistributedLock.OUTCOME_SUCCESS
    )).isEqualTo(1);
    assertThat(timerCount(
        meterRegistry,
        DistributedLock.TASK,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM,
        DistributedLock.OUTCOME_SUCCESS
    )).isEqualTo(1);
    assertThat(counterValue(
        meterRegistry,
        DistributedLock.RELEASE,
        DistributedLock.LOCK_NAME_PAYMENT_CONFIRM,
        DistributedLock.OUTCOME_SUCCESS
    )).isEqualTo(1.0);
  }

  @Test
  void shouldNormalizeUnknownLockName() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    MicrometerDistributedLockMetricRecorder recorder =
        new MicrometerDistributedLockMetricRecorder(meterRegistry);

    recorder.recordAcquireConflict(
        DistributedLock.LOCK_TYPE_REDIS,
        "lock:custom:abc",
        Duration.ofMillis(3)
    );

    assertThat(counterValue(
        meterRegistry,
        DistributedLock.ACQUIRE,
        DistributedLock.LOCK_NAME_UNKNOWN,
        DistributedLock.OUTCOME_CONFLICT
    )).isEqualTo(1.0);
  }

  private double counterValue(
      SimpleMeterRegistry meterRegistry,
      String metricName,
      String lockName,
      String outcome
  ) {
    return meterRegistry.get(metricName)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .counter()
        .count();
  }

  private long timerCount(
      SimpleMeterRegistry meterRegistry,
      String metricName,
      String lockName,
      String outcome
  ) {
    return meterRegistry.get(metricName)
        .tag(DistributedLock.TAG_LOCK_TYPE, DistributedLock.LOCK_TYPE_REDIS)
        .tag(DistributedLock.TAG_LOCK_NAME, lockName)
        .tag(DistributedLock.TAG_OUTCOME, outcome)
        .timer()
        .count();
  }
}
