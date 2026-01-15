package com.dragons.domain.subscription;

public interface SubscriptionRepository {
  void save(Subscription subscription);

  boolean exists(String holderName);
}
