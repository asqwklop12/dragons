package com.dragons.infra.jpa.coupon;

import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaIssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {
  @EntityGraph(attributePaths = "coupon")
  Optional<IssuedCoupon> findByIdAndUserIdAndDeletedAtIsNull(Long issuedCouponId, Long userId);

  @EntityGraph(attributePaths = "coupon")
  List<IssuedCoupon> findAllByUserIdAndDeletedAtIsNullOrderByIssuedAtDesc(Long userId);

  @EntityGraph(attributePaths = "coupon")
  List<IssuedCoupon> findAllByUserIdAndStatusAndExpiredAtGreaterThanEqualAndDeletedAtIsNullOrderByIssuedAtDesc(
      Long userId,
      IssuedCouponStatus status,
      ZonedDateTime now);
}
