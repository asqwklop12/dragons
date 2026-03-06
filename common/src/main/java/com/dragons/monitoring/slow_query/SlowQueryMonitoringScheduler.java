package com.dragons.monitoring.slow_query;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "slow-query.monitor.enabled", havingValue = "true", matchIfMissing = true)
public class SlowQueryMonitoringScheduler {

  private final SlowQueryMonitor slowQueryMonitor;

  @Scheduled(
      fixedDelayString = "${slow-query.monitor.fixed-delay-ms:5000}",
      initialDelayString = "${slow-query.monitor.initial-delay-ms:3000}"
  )
  public void monitor() {
    try {
      slowQueryMonitor.collect();
    } catch (IOException e) {
      log.warn("슬로우쿼리 모니터링 실행 중 오류가 발생했습니다. message={}", e.getMessage(), e);
    }
  }
}
