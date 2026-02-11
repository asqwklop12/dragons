package com.dragons.config;

import com.dragons.slow_query.FileOffsetStore;
import com.dragons.slow_query.SlowQueryBlockParser;
import com.dragons.slow_query.SlowQueryEventHandler;
import com.dragons.slow_query.SlowQueryFileReader;
import com.dragons.slow_query.SlowQueryMonitor;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SlowQueryMonitoringConfig {


  @Value("${slow-query.log-path}")
  private String logPath;


  @Bean
  public FileOffsetStore fileOffsetStore() {
    return new FileOffsetStore();
  }


  @Bean
  public SlowQueryFileReader slowQueryFileReader(FileOffsetStore offsetStore) {
    return new SlowQueryFileReader(Path.of(logPath), offsetStore);
  }


  @Bean
  public SlowQueryBlockParser slowQueryBlockParser() {
    return new SlowQueryBlockParser();
  }


  @Bean
  public SlowQueryEventHandler slowQueryEventHandler() {
    return event -> {
      log.info("슬로쿼리 감지: queryTime={}초, sql={}", event.queryTime(), event.sql());
    };
  }

  @Bean
  public SlowQueryMonitor slowQueryMonitor(
      SlowQueryFileReader reader,
      SlowQueryBlockParser parser,
      SlowQueryEventHandler handler) {
    return new SlowQueryMonitor(reader, parser, handler);
  }
}
