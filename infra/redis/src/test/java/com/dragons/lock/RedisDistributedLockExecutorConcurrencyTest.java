package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dragons.constant.Constants.Metric.DistributedLock;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.monitoring.lock.DistributedLockMetricRecorder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.ObjectProvider;

class RedisDistributedLockExecutorConcurrencyTest extends RedisIntegrationTestSupport {

  private DistributedLockMetricRecorder metricRecorder;
  private RedisDistributedLockExecutor executor;

  @BeforeEach
  void setUp() {
    @SuppressWarnings("unchecked")
    ObjectProvider<DistributedLockFactory> factoryProvider = mock(ObjectProvider.class);
    metricRecorder = mock(DistributedLockMetricRecorder.class);
    executor = new RedisDistributedLockExecutor(redisTemplate, factoryProvider, metricRecorder);
  }

  @ParameterizedTest(name = "[{index}] workers={0}, taskDurationMs={1}")
  @MethodSource("concurrencyScenarios")
  @DisplayName("동시에 같은 Redis 락을 잡으려 하면 하나의 작업만 실행된다")
  void executeWithLock_concurrency(
      int workerCount,
      long taskDurationMillis
  ) throws Exception {
    // given
    String key = nextLockKey("lock:payment-confirm");
    AtomicInteger taskRunCount = new AtomicInteger();
    LockOptions options = LockOptions.of(Duration.ofSeconds(5));

    // when
    List<Optional<String>> results = runSimultaneously(
        workerCount,
        () -> executor.executeWithLock(
            key,
            options,
            () -> {
              taskRunCount.incrementAndGet();
              sleep(taskDurationMillis);
              return "locked";
            }
        )
    );

    // then
    long acquiredCount = results.stream()
        .filter(Optional::isPresent)
        .count();
    long conflictCount = results.size() - acquiredCount;

    assertThat(acquiredCount).isEqualTo(1);
    assertThat(conflictCount).isEqualTo(workerCount - 1L);
    assertThat(taskRunCount.get()).isEqualTo(1);
    assertThat(redisTemplate.hasKey(key)).isFalse();

    verify(metricRecorder, times(1)).recordAcquireSuccess(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq(key),
        any(Duration.class)
    );
    verify(metricRecorder, times(workerCount - 1)).recordAcquireConflict(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq(key),
        any(Duration.class)
    );
    verify(metricRecorder, times(1)).recordTaskSuccess(
        eq(DistributedLock.LOCK_TYPE_REDIS),
        eq(key),
        any(Duration.class)
    );
    verify(metricRecorder, times(1)).recordReleaseSuccess(
        DistributedLock.LOCK_TYPE_REDIS,
        key
    );
    verify(metricRecorder, never()).recordTaskFailure(any(), any(), any());
  }

  private static Stream<Arguments> concurrencyScenarios() {
    return Stream.of(
        Arguments.of(2, 150L),
        Arguments.of(6, 300L),
        Arguments.of(10, 500L),
        Arguments.of(20, 500L),
        Arguments.of(30, 500L)
    );
  }

  private List<Optional<String>> runSimultaneously(
      int workerCount,
      Callable<Optional<String>> task
  ) throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
    CountDownLatch ready = new CountDownLatch(workerCount);
    CountDownLatch start = new CountDownLatch(1);

    try {
      List<Future<Optional<String>>> futures = new ArrayList<>();

      for (int index = 0; index < workerCount; index++) {
        futures.add(executorService.submit(() -> {
          ready.countDown();
          assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
          return task.call();
        }));
      }

      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      start.countDown();

      List<Optional<String>> results = new ArrayList<>();
      for (Future<Optional<String>> future : futures) {
        results.add(future.get(10, TimeUnit.SECONDS));
      }
      return results;
    } finally {
      executorService.shutdownNow();
    }
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Thread interrupted during concurrency test", exception);
    }
  }
}
