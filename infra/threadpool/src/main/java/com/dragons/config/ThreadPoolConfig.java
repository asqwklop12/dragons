package com.dragons.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${spring.task.execution.pool.keep-alive:30s}")
  private String keepAlive;

  @Bean(name = "taskExecutor")
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // YAML 설정값 적용
    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.setKeepAliveSeconds(parseKeepAlive(keepAlive));

    // 거부 정책 설정 (CallerRunsPolicy 권장)
    // 큐가 가득 찼을 때 호출한 스레드에서 직접 실행하여 back-pressure 제공
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // 애플리케이션 종료 시 작업 완료 대기
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(20);

    executor.initialize();
    return executor;
  }

  private int parseKeepAlive(String keepAlive) {
    // "30s" 형식을 초 단위로 파싱
    if (keepAlive.endsWith("s")) {
      return Integer.parseInt(keepAlive.substring(0, keepAlive.length() - 1));
    }
    return 30; // 기본값
  }
}
