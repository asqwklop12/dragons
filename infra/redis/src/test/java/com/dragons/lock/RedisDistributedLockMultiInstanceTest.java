package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;

import com.dragons.domain.lock.LockOptions;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisDistributedLockMultiInstanceTest extends RedisIntegrationTestSupport {

  @Test
  @DisplayName("10개 인스턴스가 같은 Redis 락을 동시에 잡으면 한 번만 획득되고 해제 후 다시 획득된다")
  void tenInstances_shouldContentAndAcquireAgainAfterRelease() throws Exception {
    int instanceCount = 10;
    String key = nextLockKey("lock:payment-confirm");
    LockOptions options = LockOptions.of(Duration.ofSeconds(5));
    AtomicInteger taskRunCount = new AtomicInteger();

    List<InstanceHarness> instances = createInstances(instanceCount);
    try {
      List<Boolean> firstWaveResults = runSimultaneously(instances, instance ->
          instance.executor.runWithLock(key, options, () -> {
            taskRunCount.incrementAndGet();
            sleep(700);
          })
      );

      assertWave(firstWaveResults, taskRunCount, 1);
      assertAggregatedMetrics(instances, instanceCount, instanceCount - 1, 1, 0);
      assertThat(redisTemplate.hasKey(key)).isFalse();

      List<Boolean> secondWaveResults = runSimultaneously(instances, instance ->
          instance.executor.runWithLock(key, options, () -> {
            taskRunCount.incrementAndGet();
            sleep(300);
          })
      );

      assertWave(secondWaveResults, taskRunCount, 2);
      assertAggregatedMetrics(instances, instanceCount * 2, (instanceCount - 1) * 2, 2, 0);
      assertThat(redisTemplate.hasKey(key)).isFalse();
    } finally {
      instances.forEach(InstanceHarness::destroy);
    }
  }

  private void assertWave(List<Boolean> results, AtomicInteger taskRunCount, int expectedTaskRuns) {
    long acquiredCount = results.stream()
        .filter(Boolean::booleanValue)
        .count();
    long conflictCount = results.size() - acquiredCount;

    assertThat(acquiredCount).isEqualTo(1);
    assertThat(conflictCount).isEqualTo(results.size() - 1L);
    assertThat(taskRunCount.get()).isEqualTo(expectedTaskRuns);
  }

  private void assertAggregatedMetrics(
      List<InstanceHarness> instances,
      double expectedAttempts,
      double expectedFailures,
      double expectedReleaseSuccess,
      double expectedReleaseFailures
  ) {
    double attempts = instances.stream()
        .mapToDouble(instance -> instance.meterRegistry.counter("redis.lock.acquire.attempts").count())
        .sum();
    double failures = instances.stream()
        .mapToDouble(instance -> instance.meterRegistry.counter("redis.lock.acquire.failures").count())
        .sum();
    double releaseSuccess = instances.stream()
        .mapToDouble(instance -> instance.meterRegistry.counter("redis.lock.release.success").count())
        .sum();
    double releaseFailures = instances.stream()
        .mapToDouble(instance -> instance.meterRegistry.counter("redis.lock.release.failures").count())
        .sum();

    assertThat(attempts).isEqualTo(expectedAttempts);
    assertThat(failures).isEqualTo(expectedFailures);
    assertThat(releaseSuccess).isEqualTo(expectedReleaseSuccess);
    assertThat(releaseFailures).isEqualTo(expectedReleaseFailures);
  }

  private List<InstanceHarness> createInstances(int instanceCount) {
    List<InstanceHarness> instances = new ArrayList<>();

    for (int index = 0; index < instanceCount; index++) {
      LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
          new RedisStandaloneConfiguration(REDIS_CONTAINER.getHost(), REDIS_CONTAINER.getFirstMappedPort())
      );
      connectionFactory.afterPropertiesSet();

      StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(connectionFactory);
      stringRedisTemplate.afterPropertiesSet();

      SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
      RedisDistributedLockExecutor executor =
          new RedisDistributedLockExecutor(stringRedisTemplate, meterRegistry);

      instances.add(new InstanceHarness(connectionFactory, meterRegistry, executor));
    }

    return instances;
  }

  private List<Boolean> runSimultaneously(
      List<InstanceHarness> instances,
      InstanceTask task
  ) throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(instances.size());
    CountDownLatch ready = new CountDownLatch(instances.size());
    CountDownLatch start = new CountDownLatch(1);

    try {
      List<Future<Boolean>> futures = new ArrayList<>();

      for (InstanceHarness instance : instances) {
        futures.add(executorService.submit(() -> {
          ready.countDown();
          assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
          return task.run(instance);
        }));
      }

      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      start.countDown();

      List<Boolean> results = new ArrayList<>();
      for (Future<Boolean> future : futures) {
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
      throw new IllegalStateException("Thread interrupted during multi-instance test", exception);
    }
  }

  @FunctionalInterface
  private interface InstanceTask {
    Boolean run(InstanceHarness instance) throws Exception;
  }

  private record InstanceHarness(
      LettuceConnectionFactory connectionFactory,
      SimpleMeterRegistry meterRegistry,
      RedisDistributedLockExecutor executor
  ) {
    void destroy() {
      connectionFactory.destroy();
      meterRegistry.close();
    }
  }
}
