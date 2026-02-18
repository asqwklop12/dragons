package com.dragons.application.subscription;

import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.lock.DistributedLockManager;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiredScheduler {
  private final Clock clock;
  private final SubscriptionRepository subscriptionRepository;
  private final DistributedLockManager distributedManager;

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  @SchedulerLock(name = "subscriptionExpiredJob",
      lockAtMostFor = "PT20S",
      lockAtLeastFor = "PT3S")
  public void expired() {
    distributedManager.executeWithLock("lock:subscription-expire", Duration.ofSeconds(30),
        () -> subscriptionRepository.updateStatusExpiredSubscription(clock));
    log.info("구독 만료 상태 업데이트 완료");
  }
}
