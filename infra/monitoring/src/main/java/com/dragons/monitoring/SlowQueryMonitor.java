package com.dragons.monitoring;

import java.io.IOException;
import java.util.List;

public class SlowQueryMonitor {
  private final SlowQueryFileReader reader;
  private final SlowQueryBlockParser parser;
  private final SlowQueryEventHandler handler;

  public SlowQueryMonitor(
      SlowQueryFileReader reader,
      SlowQueryBlockParser parser,
      SlowQueryEventHandler handler
  ) {
    this.reader = reader;
    this.parser = parser;
    this.handler = handler;
  }

  public void collect() throws IOException {
    List<String> lines = reader.readNewLines();
    List<SlowQueryEvent> events = parser.parse(lines);

    for (SlowQueryEvent event : events) {
      handler.handle(event);
    }
  }
}
