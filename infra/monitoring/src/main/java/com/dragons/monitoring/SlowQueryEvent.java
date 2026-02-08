package com.dragons.monitoring;

import java.time.Instant;

public record SlowQueryEvent(
    Instant time,
    double queryTime,
    double lockTime,
    long rowsExamined,
    String sql
) {}
