package com.dragons.monitoring.slow_query;

@FunctionalInterface
public interface SlowQueryEventHandler {
  void handle(SlowQueryEvent event);
}
