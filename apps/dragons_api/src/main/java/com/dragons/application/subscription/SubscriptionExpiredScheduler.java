package com.dragons.application.subscription;

import com.dragons.constant.Constants.LockKey;
import com.dragons.domain.distribute.DistributedLockFactory;
import com.dragons.domain.distribute.LockOptions;
import com.dragons.domain.distribute.LockType;
import com.dragons.domain.subscription.SubscriptionRepository;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubscriptionExpiredScheduler {
  private final Clock clock;
  private final SubscriptionRepository subscriptionRepository;
  private final DistributedLockFactory factory;

  @Scheduled(cron = "0 0 0 * * *")
  public void expired() {
    factory.get(LockType.REDIS).executeWithLock(LockKey.REDIS_EXPIRED_SUBSCRIPTION,
        LockOptions.of(Duration.ofSeconds(30)),
        () -> {
          subscriptionRepository.updateStatusExpiredSubscription(clock);
          return null;
        });
  }
}
