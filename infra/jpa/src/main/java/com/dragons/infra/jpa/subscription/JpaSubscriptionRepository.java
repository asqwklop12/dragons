package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long> {

  boolean existsSubscriptionByHolderName(String holderName);
}
