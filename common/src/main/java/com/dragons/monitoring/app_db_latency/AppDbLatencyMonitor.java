package com.dragons.monitoring.app_db_latency;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppDbLatencyMonitor {
  private final AppDbLatencySqlParser parser;
  private final AppDbLatencyEventHandler handler;
  private final boolean enabled;
  private final long slowThresholdMillis;

  public AppDbLatencyMonitor(
      AppDbLatencySqlParser parser,
      AppDbLatencyEventHandler handler,
      boolean enabled,
      long slowThresholdMillis
  ) {
    this.parser = parser;
    this.handler = handler;
    this.enabled = enabled;
    this.slowThresholdMillis = slowThresholdMillis;
  }

  public void collect(String rawSql, String traceId, long elapsedMillis) {
    if (!enabled || elapsedMillis < slowThresholdMillis) {
      return;
    }

    try {
      String normalizedSql = parser.toFingerprint(rawSql);
      double queryTimeSeconds = elapsedMillis / 1000.0;

      handler.handle(new AppDbLatencyEvent(
          Instant.now(),
          queryTimeSeconds,
          traceId,
          normalizedSql
      ));
    } catch (RuntimeException e) {
      log.warn("앱 DB 지연 모니터링 수집 실패", e);
    }

  }
}
