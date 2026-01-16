package com.dragons.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

  @Value("${spring.task.execution.pool.core-size:4}")
  private int corePoolSize;

  @Value("${spring.task.execution.pool.max-size:8}")
  private int maxPoolSize;

  @Value("${spring.task.execution.pool.queue-capacity:200}")
  private int queueCapacity;

  @Value("${spring.task.execution.thread-name-prefix:app-async-}")
  private String threadNamePrefix;

  @DurationUnit(ChronoUnit.SECONDS)
  @Value("${spring.task.execution.pool.keep-alive:30s}")
  private Duration keepAlive;

  @Bean(name = "taskExecutor")
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // YAML 설정값 적용
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.setKeepAliveSeconds(Math.toIntExact(keepAlive.getSeconds()));

    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

    // 애플리케이션 종료 시 작업 완료 대기
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(20);

    return executor;
  }

}
