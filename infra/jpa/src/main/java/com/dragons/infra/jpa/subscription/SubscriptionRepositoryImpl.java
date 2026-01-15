package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
  private final JpaSubscriptionRepository subscriptionRepository;

  @Override
  public void save(Subscription subscription) {
    subscriptionRepository.save(subscription);
  }

  @Override
  public boolean exists(String holderName) {
    return subscriptionRepository.existsSubscriptionByHolderName(holderName);

  }
}
