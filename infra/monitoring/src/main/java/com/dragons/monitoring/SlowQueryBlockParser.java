package com.dragons.monitoring;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlowQueryBlockParser {
  public List<SlowQueryEvent> parse(List<String> lines) {
    List<List<String>> blocks = splitBlocks(lines);
    List<SlowQueryEvent> events = new ArrayList<>();

    for (List<String> block : blocks) {
      parseBlock(block).ifPresent(events::add);
    }

    return events;
  }

  private List<List<String>> splitBlocks(List<String> lines) {
    List<List<String>> blocks = new ArrayList<>();
    List<String> current = new ArrayList<>();

    for (String line : lines) {
      if (line.startsWith("# Time") && !current.isEmpty()) {
        blocks.add(current);
        current = new ArrayList<>();
      }
      current.add(line);
    }

    if (!current.isEmpty()) {
      blocks.add(current);
    }

    return blocks;
  }
  private Optional<SlowQueryEvent> parseBlock(List<String> block) {
    try {
      Instant time = null;
      double queryTime = 0;
      double lockTime = 0;
      long rowsExamined = 0;
      StringBuilder sql = new StringBuilder();

      for (String line : block) {
        if (line.startsWith("# Time:")) {
          time = Instant.parse(line.substring(7).trim());
        } else if (line.contains("Query_time")) {
          queryTime = extractDouble(line, "Query_time");
          lockTime = extractDouble(line, "Lock_time");
          rowsExamined = extractLong(line, "Rows_examined");
        } else if (!line.startsWith("#") && !line.startsWith("SET timestamp")) {
          sql.append(line).append("\n");
        }
      }

      if (time == null || sql.isEmpty()) {
        return Optional.empty();
      }

      return Optional.of(
          new SlowQueryEvent(
              time,
              queryTime,
              lockTime,
              rowsExamined,
              sql.toString().trim()
          )
      );

    } catch (Exception e) {
      return Optional.empty(); // 파싱 실패는 조용히 무시
    }
  }

  private double extractDouble(String line, String key) {
    return Double.parseDouble(
        extractValue(line, key)
    );
  }

  private long extractLong(String line, String key) {
    return Long.parseLong(
        extractValue(line, key)
    );
  }

  private String extractValue(String line, String key) {
    int idx = line.indexOf(key);
    if (idx == -1) return "0";

    String part = line.substring(idx + key.length());
    return part.split("\\s+")[1];
  }
}
