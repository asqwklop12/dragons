package com.dragons.application.coupon.dto;

public record CouponUseCommand(
    Long issuedCouponId,
    Long userId,
    Long orderId,
    Integer orderAmount
) {
}
