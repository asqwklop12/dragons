package com.dragons.infra.jpa.coupon;

import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponRepository;
import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class IssuedCouponRepositoryImpl implements IssuedCouponRepository {
  private final JpaIssuedCouponRepository jpaIssuedCouponRepository;

  @Override
  public IssuedCoupon store(IssuedCoupon issuedCoupon) {
    return jpaIssuedCouponRepository.save(issuedCoupon);
  }

  @Override
  public Optional<IssuedCoupon> readOwnedCoupon(Long issuedCouponId, Long userId) {
    return jpaIssuedCouponRepository.findByIdAndUserIdAndDeletedAtIsNull(issuedCouponId, userId);
  }

  @Override
  public List<IssuedCoupon> readUserCoupons(Long userId) {
    return jpaIssuedCouponRepository.findAllByUserIdAndDeletedAtIsNullOrderByIssuedAtDesc(userId);
  }

  @Override
  public List<IssuedCoupon> readUsableUserCoupons(Long userId, ZonedDateTime now) {
    return jpaIssuedCouponRepository
        .findAllByUserIdAndStatusAndExpiredAtGreaterThanEqualAndDeletedAtIsNullOrderByIssuedAtDesc(
            userId,
            IssuedCouponStatus.ISSUED,
            now);
  }
}
