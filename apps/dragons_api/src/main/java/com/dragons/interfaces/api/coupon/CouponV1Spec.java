package com.dragons.interfaces.api.coupon;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.coupon.dto.CouponV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Coupon V1 API", description = "쿠폰 발급/사용 API")
public interface CouponV1Spec {

  @Operation(summary = "쿠폰 생성", description = "이벤트 쿠폰을 생성합니다.")
  ApiResponse<CouponV1Dto.Create.Response> createCoupon(CouponV1Dto.Create.Request request);

  @Operation(summary = "발급 가능 쿠폰 조회", description = "현재 발급 가능한 쿠폰 목록을 조회합니다.")
  ApiResponse<CouponV1Dto.Available.Response> getAvailableCoupons();

  @Operation(summary = "쿠폰 발급", description = "선착순 쿠폰 발급을 요청합니다.")
  ApiResponse<CouponV1Dto.Issue.Response> issueCoupon(Long couponId, CouponV1Dto.Issue.Request request);

  @Operation(summary = "쿠폰 재고 조회", description = "특정 쿠폰의 남은 수량을 조회합니다.")
  ApiResponse<CouponV1Dto.Stock.Response> getStock(Long couponId);

  @Operation(summary = "내 쿠폰 조회", description = "로그인한 사용자가 보유한 전체 쿠폰을 조회합니다.")
  ApiResponse<CouponV1Dto.UserCoupon.Response> getUserCoupons(
      @Parameter(hidden = true) String email);

  @Operation(summary = "내 사용 가능 쿠폰 조회", description = "로그인한 사용자가 보유한 사용 가능 쿠폰을 조회합니다.")
  ApiResponse<CouponV1Dto.UserCoupon.Response> getUsableCoupons(
      @Parameter(hidden = true) String email);

  @Operation(summary = "쿠폰 사용", description = "로그인한 사용자 기준으로 발급된 쿠폰을 사용 처리합니다.")
  ApiResponse<CouponV1Dto.Use.Response> useCoupon(
      @Parameter(hidden = true) String email,
      Long issuedCouponId,
      CouponV1Dto.Use.Request request);
}
