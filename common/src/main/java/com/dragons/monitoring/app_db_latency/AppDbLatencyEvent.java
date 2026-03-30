package com.dragons.monitoring.app_db_latency;

import java.time.Instant;

public record AppDbLatencyEvent(
    Instant time,
    double queryTime,
    String traceId,
    String sql
) {
}
