package com.dragons.application.subscription;

import com.dragons.domain.subscription.SubscriptionRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiredScheduler {
  private final Clock clock;
  private final SubscriptionRepository subscriptionRepository;

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void expired() {
    subscriptionRepository.updateStatusExpiredSubscription(clock);
    log.info("구독 만료 상태 업데이트 완료");
  }
}
