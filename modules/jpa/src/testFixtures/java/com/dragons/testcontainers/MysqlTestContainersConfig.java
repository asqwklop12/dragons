package com.dragons.testcontainers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Configuration
@Testcontainers
public class MysqlTestContainersConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext context) {
    MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test");
    mysql.start();

    // 시스템 프로퍼티 직접 주입
    TestPropertyValues.of(
        "MYSQL_HOST=" + mysql.getHost(),
        "MYSQL_PORT=" + mysql.getFirstMappedPort(),
        "datasource.mysql-jpa.main.jdbc-url=" + mysql.getJdbcUrl(),
        "datasource.mysql-jpa.main.username=" + mysql.getUsername(),
        "datasource.mysql-jpa.main.password=" + mysql.getPassword()
    ).applyTo(context.getEnvironment());
  }
}
