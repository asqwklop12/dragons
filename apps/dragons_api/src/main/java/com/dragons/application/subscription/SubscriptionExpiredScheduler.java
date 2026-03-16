package com.dragons.application.subscription;

import com.dragons.constant.Constants.Lock;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import com.dragons.domain.subscription.SubscriptionRepository;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionExpiredScheduler {
  private final Clock clock;
  private final SubscriptionRepository subscriptionRepository;
  private final DistributedLockFactory factory;

  @Scheduled(cron = "0 0 0 * * *")
  public void expired() {
    factory.get(LockType.SHEDLOCK).runWithLock(
        Lock.LOCK_EXPIRED_SUBSCRIPTION,
        LockOptions.of(Duration.ofSeconds(30), Duration.ofSeconds(3)),
        () -> subscriptionRepository.updateStatusExpiredSubscription(clock)
    );
  }
}
