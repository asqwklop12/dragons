package com.dragons.testcontainers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;

public class MysqlTestContainersConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("test")
      .withUsername("test")
      .withPassword("test");

  static {
    mysql.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    // 시스템 프로퍼티 직접 주입
    TestPropertyValues.of(
        "datasource.mysql-jpa.main.jdbc-url=" + mysql.getJdbcUrl(),
        "datasource.mysql-jpa.main.username=" + mysql.getUsername(),
        "datasource.mysql-jpa.main.password=" + mysql.getPassword()
    ).applyTo(context.getEnvironment());
  }
}
