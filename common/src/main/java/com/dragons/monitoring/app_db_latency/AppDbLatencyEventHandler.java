package com.dragons.monitoring.app_db_latency;

@FunctionalInterface
public interface AppDbLatencyEventHandler {
  void handle(AppDbLatencyEvent event);
}
