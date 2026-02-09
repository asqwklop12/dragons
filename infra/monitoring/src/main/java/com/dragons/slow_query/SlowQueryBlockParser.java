package com.dragons.slow_query;

import com.dragons.constant.Constants;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
      if (line.startsWith("# Time")) {
        if (!current.isEmpty()) {
          blocks.add(current);
        }
        current = new ArrayList<>();
        current.add(line);
      } else {
        current.add(line);
      }
      // current가 null이면 아직 "# Time" 헤더를 만나지 않았으므로 라인을 무시
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
          time = parseTimestamp(line.substring(7).trim());
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
      log.warn("슬로우 쿼리 블록 파싱 실패: {}", e.getMessage(), e);
      return Optional.empty(); // 파싱 실패는 조용히 무시
    }
  }

  private Instant parseTimestamp(String timestamp) {
    try {
      return Instant.parse(timestamp);
    } catch (Exception e) {
      // MySQL 5.6/5.7 레거시 형식 시도
      try {
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, Constants.LEGACY_FORMATTER);
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
      } catch (Exception ex) {
        throw new IllegalArgumentException("Unable to parse timestamp: " + timestamp, ex);
      }
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
    if (idx == -1) {
      throw new IllegalArgumentException("Key not found: " + key);
    }

    String part = line.substring(idx + key.length()).trim();
    if (part.startsWith(":")) {
      part = part.substring(1).trim();
    }
    String[] tokens = part.split("\\s+");
    if (tokens.length < 1 || tokens[0].isEmpty()) {
      throw new IllegalArgumentException("Value not found for key: " + key);
    }
    return tokens[0];
  }
}
