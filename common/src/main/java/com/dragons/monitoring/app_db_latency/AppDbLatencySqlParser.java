package com.dragons.monitoring.app_db_latency;

public class AppDbLatencySqlParser {

  public String normalize(String sql) {
    if (sql == null) {
      return "";
    }
    return sql.trim().replaceAll("\\s+", " ");
  }

  public String toFingerprint(String sql) {
    String normalized = normalize(sql);
    return normalized
        .replaceAll("'[^']*'", "?")
        .replaceAll("\\b\\d+\\b", "?");
  }
}
