package com.dragons.infra.jpa.coupon;

import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaCouponRepository extends JpaRepository<Coupon, Long> {
  @Query("""
      SELECT c
      FROM Coupon c
      WHERE c.deletedAt IS NULL
      AND c.status = :status
      AND c.startDate <= :now
      AND c.endDate >= :now
      """)
  List<Coupon> findAllIssuable(@Param("now") ZonedDateTime now,
                               @Param("status") CouponStatus status);

  Optional<Coupon> findByIdAndDeletedAtIsNull(Long couponId);
}
