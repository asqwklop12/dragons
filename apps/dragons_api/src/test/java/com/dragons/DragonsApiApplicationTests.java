package com.dragons;

import com.dragons.testcontainers.MysqlTestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = MysqlTestContainersConfig.class)
class DragonsApiApplicationTests {

  @Test
  void contextLoads() {
  }

}
