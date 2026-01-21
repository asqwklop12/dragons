package com.dragons.domain.subscription;

import java.util.Optional;

public interface SubscriptionRepository {
  Subscription save(Subscription subscription);

  boolean exists(String holderName);

  Optional<Subscription> findExpiredByHolderName(String holderName);

  Optional<Subscription> findByHolderName(String holderName);
}
