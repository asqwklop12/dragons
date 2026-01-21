package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class SubscriptionRepositoryImpl implements SubscriptionRepository {
  private final JpaSubscriptionRepository subscriptionRepository;

  @Override
  public Subscription save(Subscription subscription) {
    return subscriptionRepository.save(subscription);
  }

  @Override
  public boolean exists(String holderName) {
    ZonedDateTime current = ZonedDateTime.now();
    return subscriptionRepository.existsSubscriptionByHolderName(holderName, current);
  }

  @Override
  public Optional<Subscription> findExpiredByHolderName(String holderName) {
    ZonedDateTime current = ZonedDateTime.now();
    return subscriptionRepository.expireSubscriptionByHolderName(holderName, current);
  }

  @Override
  public Optional<Subscription> findByHolderName(String holderName) {
    return subscriptionRepository.findByHolderName(holderName);
  }

  @Override
  public void updateStatusExpiredSubscription() {
    subscriptionRepository.updateStatusExpiredSubscription(ZonedDateTime.now());
  }
}
