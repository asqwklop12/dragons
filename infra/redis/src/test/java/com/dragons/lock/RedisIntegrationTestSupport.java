package com.dragons.lock;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

abstract class RedisIntegrationTestSupport {

  private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.2-alpine");
  private static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(REDIS_IMAGE).withExposedPorts(6379);

  static {
    REDIS_CONTAINER.start();
  }

  protected LettuceConnectionFactory connectionFactory;
  protected StringRedisTemplate redisTemplate;

  @BeforeEach
  void setUpRedis() {
    if (!REDIS_CONTAINER.isRunning()) {
      REDIS_CONTAINER.start();
    }

    connectionFactory = new LettuceConnectionFactory(
        new RedisStandaloneConfiguration(
            REDIS_CONTAINER.getHost(),
            REDIS_CONTAINER.getFirstMappedPort()
        )
    );
    connectionFactory.afterPropertiesSet();

    redisTemplate = new StringRedisTemplate(connectionFactory);
    redisTemplate.afterPropertiesSet();
    flushDb();
  }

  @AfterEach
  void tearDownRedis() {
    if (connectionFactory != null) {
      connectionFactory.destroy();
    }
  }

  @AfterAll
  static void tearDownContainer() {
    REDIS_CONTAINER.stop();
  }

  protected void flushDb() {
    RedisConnection connection = connectionFactory.getConnection();
    try {
      connection.serverCommands().flushDb();
    } finally {
      connection.close();
    }
  }

  protected String nextLockKey(String prefix) {
    return prefix + ":" + UUID.randomUUID();
  }
}
