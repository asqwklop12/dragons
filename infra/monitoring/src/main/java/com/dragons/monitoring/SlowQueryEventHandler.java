package com.dragons.monitoring;

public interface SlowQueryEventHandler {
  void handle(SlowQueryEvent event);
}
