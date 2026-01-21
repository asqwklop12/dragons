package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long> {

  @Query("""
      SELECT COUNT(s.holderName) > 0 FROM
      Subscription s
      WHERE s.holderName= :holderName
      AND s.expireDate > :time
      """)
  boolean existsSubscriptionByHolderName(@Param("holderName") String holderName,
                                         @Param("time") ZonedDateTime time);

  @Query("""
      SELECT s FROM
      Subscription s
      WHERE s.holderName = :holderName
      AND (s.status = 'EXPIRE' OR
           s.expireDate < :time
          )
      """)
  Optional<Subscription> expireSubscriptionByHolderName(@Param("holderName") String holderName,
                                                        @Param("time") ZonedDateTime time);
}
