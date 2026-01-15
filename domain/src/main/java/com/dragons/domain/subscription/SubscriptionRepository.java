package com.dragons.domain.subscription;

public interface SubscriptionRepository {
  Subscription save(Subscription subscription);

  boolean exists(String holderName);
}
