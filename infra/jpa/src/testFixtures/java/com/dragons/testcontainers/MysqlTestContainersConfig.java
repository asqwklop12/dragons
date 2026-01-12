package com.dragons.testcontainers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

public class MysqlTestContainersConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("dragons")
      .withUsername("test")
      .withPassword("test");

  static {
    mysql.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    // 시스템 프로퍼티 직접 주입
    TestPropertyValues.of(
        "MYSQL_HOST=" + mysql.getHost(),
        "MYSQL_PORT=" + mysql.getFirstMappedPort(),
        "MYSQL_USER=" + mysql.getUsername(),
        "MYSQL_PWD=" + mysql.getPassword()).applyTo(context.getEnvironment());
  }
}
