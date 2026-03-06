package com.dragons.config.jpa;


import com.dragons.monitoring.app_db_latency.AppDbLatencyDataSource;
import com.dragons.monitoring.app_db_latency.AppDbLatencyMonitor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
class DataSourceConfig {

  @Bean
  @ConfigurationProperties(prefix = "datasource.mysql-jpa.primary")
  HikariConfig primaryHikariConfig() {
    return new HikariConfig();
  }

  @Bean
  DataSource primaryDataSource(
      @Qualifier("primaryHikariConfig") HikariConfig config) {
    return new HikariDataSource(config);
  }

  @Bean
  @ConfigurationProperties(prefix = "datasource.mysql-jpa.replica")
  HikariConfig replicaHikariConfig() {
    return new HikariConfig();
  }

  @Bean
  DataSource replicaDataSource(
      @Qualifier("replicaHikariConfig") HikariConfig config) {
    return new HikariDataSource(config);
  }

  @Bean
  @Primary
  public DataSource dataSource(
      @Qualifier("routingDataSource") DataSource routing,
      AppDbLatencyMonitor appDbLatencyMonitor
  ) {
    DataSource lazyProxy = new LazyConnectionDataSourceProxy(routing);
    return new AppDbLatencyDataSource(lazyProxy, appDbLatencyMonitor);
  }

  @Bean
  public DataSource routingDataSource(
      @Qualifier("primaryDataSource") DataSource primary,
      @Qualifier("replicaDataSource") DataSource replica) {

    Map<Object, Object> targetDataSources = new HashMap<>();
    targetDataSources.put("PRIMARY", primary);
    targetDataSources.put("REPLICA", replica);

    RoutingDataSource routingDataSource = new RoutingDataSource();
    routingDataSource.setTargetDataSources(targetDataSources);
    routingDataSource.setDefaultTargetDataSource(primary);

    return routingDataSource;
  }
}
