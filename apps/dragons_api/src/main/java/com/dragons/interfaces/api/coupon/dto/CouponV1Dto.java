package com.dragons.interfaces.api.coupon.dto;

import com.dragons.domain.coupon.CouponType;
import com.dragons.domain.coupon.IssuedCouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "CouponV1Dto", description = "쿠폰 API v1 DTO")
public class CouponV1Dto {

  private CouponV1Dto() {
  }

  public static class Available {

    public record Coupon(
        Long couponId,
        String name,
        String description,
        CouponType couponType,
        Integer discountValue,
        Integer minOrderAmount,
        Integer maxDiscountAmount,
        Integer remainingQuantity,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
    }

    public record Response(List<Coupon> coupons) {
    }
  }

  public static class Issue {

    public record Request(
        @NotNull @Schema(description = "유저 ID", example = "1") Long userId
    ) {
    }

    public record Response(
        Long issuedCouponId,
        Long couponId,
        Long userId,
        IssuedCouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
    ) {
    }
  }

  public static class Stock {

    public record Response(
        Long couponId,
        Integer remainingQuantity
    ) {
    }
  }

  public static class UserCoupon {

    public record Item(
        Long issuedCouponId,
        Long couponId,
        String couponName,
        IssuedCouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt,
        LocalDateTime usedAt
    ) {
    }

    public record Response(List<Item> coupons) {
    }
  }

  public static class Use {

    public record Request(
        @NotNull @Schema(description = "유저 ID", example = "1") Long userId,
        @NotNull @Schema(description = "주문 ID", example = "101") Long orderId,
        @NotNull @Positive @Schema(description = "주문 금액", example = "30000") Integer orderAmount
    ) {
    }

    public record Response(
        Long issuedCouponId,
        Long orderId,
        Integer discountAmount,
        IssuedCouponStatus status,
        LocalDateTime usedAt
    ) {
    }
  }
}
