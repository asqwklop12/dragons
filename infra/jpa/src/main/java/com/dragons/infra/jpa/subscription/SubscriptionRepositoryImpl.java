package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
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
    return subscriptionRepository.existsSubscriptionByHolderName(holderName);
  }
}
