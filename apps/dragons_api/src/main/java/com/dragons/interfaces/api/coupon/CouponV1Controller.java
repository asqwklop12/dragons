package com.dragons.interfaces.api.coupon;

import com.dragons.application.coupon.CouponService;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.dto.CouponUseCommand;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.coupon.dto.CouponV1Dto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponV1Controller {
  private final CouponService couponService;

  @GetMapping("/available")
  public ApiResponse<CouponV1Dto.Available.Response> getAvailableCoupons() {
    var result = couponService.getAvailableCoupons();
    return ApiResponse.success(new CouponV1Dto.Available.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.Available.Coupon(
                coupon.couponId(),
                coupon.name(),
                coupon.description(),
                coupon.couponType(),
                coupon.discountValue(),
                coupon.minOrderAmount(),
                coupon.maxDiscountAmount(),
                coupon.remainingQuantity(),
                coupon.startDate(),
                coupon.endDate()))
            .toList()));
  }

  @PostMapping("/{couponId}/issue")
  public ApiResponse<CouponV1Dto.Issue.Response> issueCoupon(
      @PathVariable Long couponId,
      @RequestBody @Valid CouponV1Dto.Issue.Request request
  ) {
    var result = couponService.issueCoupon(new CouponIssueCommand(couponId, request.userId()));
    return ApiResponse.success(new CouponV1Dto.Issue.Response(
        result.issuedCouponId(),
        result.couponId(),
        result.userId(),
        result.status(),
        result.issuedAt(),
        result.expiredAt()));
  }

  @GetMapping("/{couponId}/stock")
  public ApiResponse<CouponV1Dto.Stock.Response> getStock(@PathVariable Long couponId) {
    var result = couponService.getStock(couponId);
    return ApiResponse.success(new CouponV1Dto.Stock.Response(result.couponId(), result.remainingQuantity()));
  }

  @GetMapping("/users/{userId}")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUserCoupons(@PathVariable Long userId) {
    var result = couponService.getUserCoupons(userId);
    return ApiResponse.success(new CouponV1Dto.UserCoupon.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.UserCoupon.Item(
                coupon.issuedCouponId(),
                coupon.couponId(),
                coupon.couponName(),
                coupon.status(),
                coupon.issuedAt(),
                coupon.expiredAt(),
                coupon.usedAt()))
            .toList()));
  }

  @GetMapping("/users/{userId}/usable")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUsableCoupons(@PathVariable Long userId) {
    var result = couponService.getUsableCoupons(userId);
    return ApiResponse.success(new CouponV1Dto.UserCoupon.Response(
        result.coupons().stream()
            .map(coupon -> new CouponV1Dto.UserCoupon.Item(
                coupon.issuedCouponId(),
                coupon.couponId(),
                coupon.couponName(),
                coupon.status(),
                coupon.issuedAt(),
                coupon.expiredAt(),
                coupon.usedAt()))
            .toList()));
  }

  @PostMapping("/{issuedCouponId}/use")
  public ApiResponse<CouponV1Dto.Use.Response> useCoupon(
      @PathVariable Long issuedCouponId,
      @RequestBody @Valid CouponV1Dto.Use.Request request
  ) {
    var result = couponService.useCoupon(new CouponUseCommand(
        issuedCouponId,
        request.userId(),
        request.orderId(),
        request.orderAmount()));
    return ApiResponse.success(new CouponV1Dto.Use.Response(
        result.issuedCouponId(),
        result.orderId(),
        result.discountAmount(),
        result.status(),
        result.usedAt()));
  }
}
