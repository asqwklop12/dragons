package com.dragons.interfaces.api.coupon;

import com.dragons.domain.coupon.IssuedCouponStatus;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.coupon.dto.CouponV1Dto;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponV1Controller {

  @GetMapping("/available")
  public ApiResponse<CouponV1Dto.Available.Response> getAvailableCoupons() {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    return ApiResponse.success(new CouponV1Dto.Available.Response(List.of()));
  }

  @PostMapping("/{couponId}/issue")
  public ApiResponse<CouponV1Dto.Issue.Response> issueCoupon(
      @PathVariable Long couponId,
      @RequestBody @Valid CouponV1Dto.Issue.Request request
  ) {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    LocalDateTime now = LocalDateTime.now();
    return ApiResponse.success(new CouponV1Dto.Issue.Response(
        null,
        couponId,
        request.userId(),
        IssuedCouponStatus.ISSUED,
        now,
        now.plusDays(7)));
  }

  @GetMapping("/{couponId}/stock")
  public ApiResponse<CouponV1Dto.Stock.Response> getStock(@PathVariable Long couponId) {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    return ApiResponse.success(new CouponV1Dto.Stock.Response(couponId, 0));
  }

  @GetMapping("/users/{userId}")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUserCoupons(@PathVariable Long userId) {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    return ApiResponse.success(new CouponV1Dto.UserCoupon.Response(List.of()));
  }

  @GetMapping("/users/{userId}/usable")
  public ApiResponse<CouponV1Dto.UserCoupon.Response> getUsableCoupons(@PathVariable Long userId) {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    return ApiResponse.success(new CouponV1Dto.UserCoupon.Response(List.of()));
  }

  @PostMapping("/{issuedCouponId}/use")
  public ApiResponse<CouponV1Dto.Use.Response> useCoupon(
      @PathVariable Long issuedCouponId,
      @RequestBody @Valid CouponV1Dto.Use.Request request
  ) {
    // TODO: 서비스 연결 전 기본 스켈레톤 응답
    return ApiResponse.success(new CouponV1Dto.Use.Response(
        issuedCouponId,
        request.orderId(),
        0,
        IssuedCouponStatus.USED,
        LocalDateTime.now()));
  }
}
