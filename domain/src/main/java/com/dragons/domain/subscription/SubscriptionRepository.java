package com.dragons.domain.subscription;

public interface SubscriptionRepository {
  void apply(Subscription subscription);

  boolean find(String holderName);
}
