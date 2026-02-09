package com.dragons.slow_query;

@FunctionalInterface
public interface SlowQueryEventHandler {
  void handle(SlowQueryEvent event);
}
