package com.dragons.infra.jpa.subscription;

import com.dragons.domain.subscription.Subscription;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            AND (s.status = 'EXPIRED' OR
                (s.expireDate < :time AND s.status <> 'WAITING')
                )
            """)
    Optional<Subscription> expireSubscriptionByHolderName(@Param("holderName") String holderName,
            @Param("time") ZonedDateTime time);

    Optional<Subscription> findByHolderName(String holderName);

    @Query("""
            SELECT COUNT(s.email) > 0 FROM
            Subscription s
            WHERE s.email= :email
            AND s.expireDate > :time
            """)
    boolean existsSubscriptionByEmail(@Param("email") String email,
            @Param("time") ZonedDateTime time);

    @Query("""
            SELECT s FROM
            Subscription s
            WHERE s.email = :email
            AND (s.status = 'EXPIRED' OR
                (s.expireDate < :time AND s.status <> 'WAITING')
                )
            """)
    Optional<Subscription> expireSubscriptionByEmail(@Param("email") String email,
            @Param("time") ZonedDateTime time);

    Optional<Subscription> findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Subscription s
            SET s.status = 'EXPIRED'
            WHERE s.status = 'ACTIVE'
            AND s.expireDate < :time
            """)
    void updateStatusExpiredSubscription(@Param("time") ZonedDateTime time);
}
