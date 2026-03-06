package com.dragons.monitoring.app_db_latency;

import java.time.Instant;

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

    String normalizedSql = parser.normalize(rawSql);
    double queryTimeSeconds = elapsedMillis / 1000.0;

    handler.handle(new AppDbLatencyEvent(
        Instant.now(),
        queryTimeSeconds,
        traceId,
        normalizedSql
    ));
  }
}
