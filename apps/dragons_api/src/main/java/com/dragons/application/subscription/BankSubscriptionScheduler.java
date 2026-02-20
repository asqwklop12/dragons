package com.dragons.application.subscription;

import com.dragons.constant.Constants.LockKey;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BankSubscriptionScheduler {
  private final BankDepositService depositService;
  private final DistributedLockFactory factory;

  @Scheduled(cron = "0 30 0 * * *")
  @Transactional
  public void subscription() {
    factory.get(LockType.SHEDLOCK).executeWithLock(
        LockKey.LOCK_BANK_DEPOSIT,
        LockOptions.of(Duration.ofSeconds(30), Duration.ofSeconds(3)),
        () -> {
          depositService.deposit();
          return null;
        }
    );
  }
}
