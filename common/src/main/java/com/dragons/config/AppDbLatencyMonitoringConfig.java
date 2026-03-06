package com.dragons.config;

import com.dragons.monitoring.app_db_latency.AppDbLatencyEventHandler;
import com.dragons.monitoring.app_db_latency.AppDbLatencyMonitor;
import com.dragons.monitoring.app_db_latency.AppDbLatencySqlParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AppDbLatencyMonitoringConfig {

  @Value("${app-db-latency.monitor.enabled:true}")
  private boolean enabled;

  @Value("${app-db-latency.monitor.slow-threshold-ms:300}")
  private long slowThresholdMillis;

  @Bean
  public AppDbLatencySqlParser appDbLatencySqlParser() {
    return new AppDbLatencySqlParser();
  }

  @Bean
  public AppDbLatencyEventHandler appDbLatencyEventHandler() {
    return event -> {
      log.info("앱 슬로쿼리 감지: queryTime={}초, sql={}", event.queryTime(), event.sql());
    };
  }

  @Bean
  public AppDbLatencyMonitor appDbLatencyMonitor(
      AppDbLatencySqlParser parser,
      AppDbLatencyEventHandler handler
  ) {
    return new AppDbLatencyMonitor(parser, handler, enabled, slowThresholdMillis);
  }
}
