package com.dragons.application.coupon.dto;

import com.dragons.domain.coupon.IssuedCouponStatus;
import java.time.LocalDateTime;
import java.util.List;

public record CouponUserCouponsResult(List<CouponItem> coupons) {
  public record CouponItem(
      Long issuedCouponId,
      Long couponId,
      String couponName,
      IssuedCouponStatus status,
      LocalDateTime issuedAt,
      LocalDateTime expiredAt,
      LocalDateTime usedAt
  ) {
  }
}
