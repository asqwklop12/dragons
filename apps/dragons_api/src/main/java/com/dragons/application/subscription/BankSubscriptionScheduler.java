package com.dragons.application.subscription;

import com.dragons.domain.payment.BankDeposit;
import com.dragons.domain.payment.BankDepositRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BankSubscriptionScheduler {
  private final BankDepositRepository depositRepository;
  private final SubscriptionRepository subscriptionRepository;
  private static final long MINIMUM_PREMIUM_AMOUNT = 9900L;

  @Scheduled(cron = "0 30 0 * * *")
  @Transactional
  public void subscription() {
    // 입금 확인을 받는다.
    List<BankDeposit> bankDeposits = depositRepository.findAllWaiting();
    for (BankDeposit bankDeposit : bankDeposits) {

      // 입금이 덜된경우 무시
      if(bankDeposit.getAmount() < MINIMUM_PREMIUM_AMOUNT) {
        continue;
      }

      // 이미 저장이 되있으면 넘어간다.
      if (subscriptionRepository.exists(bankDeposit.getHolder())) {
        continue;
      }
      subscriptionRepository.save(
          Subscription.apply(bankDeposit.getHolder(), PlanType.PREMIUM.name(), Status.ACTIVE.name()));

      bankDeposit.check();
    }
  }
}
