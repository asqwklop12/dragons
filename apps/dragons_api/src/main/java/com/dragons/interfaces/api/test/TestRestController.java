package com.dragons.interfaces.api.test;

import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestRestController {
  private final DistributedLockFactory factory;

  @GetMapping
  public void test() {
    factory.get(LockType.REDIS).executeWithLock("test:key",
        LockOptions.of(Duration.of(20, ChronoUnit.SECONDS)), () -> null);
  }
}
