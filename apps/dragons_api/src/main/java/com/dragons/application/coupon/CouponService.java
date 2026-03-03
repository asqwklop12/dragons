package com.dragons.application.coupon;

import com.dragons.application.coupon.dto.CouponAvailableResult;
import com.dragons.application.coupon.dto.CouponIssueCommand;
import com.dragons.application.coupon.dto.CouponIssueResult;
import com.dragons.application.coupon.dto.CouponStockResult;
import com.dragons.application.coupon.dto.CouponUserCouponsResult;
import com.dragons.application.coupon.dto.CouponUseCommand;
import com.dragons.application.coupon.dto.CouponUseResult;
import com.dragons.domain.coupon.Coupon;
import com.dragons.domain.coupon.CouponRepository;
import com.dragons.domain.coupon.CouponType;
import com.dragons.domain.coupon.IssuedCoupon;
import com.dragons.domain.coupon.IssuedCouponRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final IssuedCouponRepository issuedCouponRepository;
  private final Clock clock;

  @Transactional(readOnly = true)
  public CouponAvailableResult getAvailableCoupons() {
    ZonedDateTime now = ZonedDateTime.now(clock);
    List<CouponAvailableResult.CouponItem> coupons = couponRepository.readIssuableCoupons(now).stream()
        .filter(coupon -> coupon.getRemainingQuantity() > 0)
        .map(coupon -> new CouponAvailableResult.CouponItem(
            coupon.getId(),
            coupon.getName(),
            coupon.getDescription(),
            coupon.getCouponType(),
            coupon.getDiscountValue(),
            coupon.getMinOrderAmount(),
            coupon.getMaxDiscountAmount(),
            coupon.getRemainingQuantity(),
            coupon.getStartDate().toOffsetDateTime(),
            coupon.getEndDate().toOffsetDateTime()))
        .toList();

    return new CouponAvailableResult(coupons);
  }

  @Transactional
  public CouponIssueResult issueCoupon(CouponIssueCommand command) {
    ZonedDateTime now = ZonedDateTime.now(clock);
    Coupon coupon = couponRepository.readActiveCoupon(command.couponId())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));

    if (!coupon.isIssuableAt(now)) {
      throw new CoreException(ErrorType.BAD_REQUEST, "발급 가능한 쿠폰이 아닙니다.");
    }

    coupon.issue(ZonedDateTime.now(clock));
    IssuedCoupon issuedCoupon = issuedCouponRepository.store(IssuedCoupon.issue(coupon, command.userId(), now));

    return new CouponIssueResult(
        issuedCoupon.getId(),
        coupon.getId(),
        command.userId(),
        issuedCoupon.getStatus(),
        toLocalDateTime(issuedCoupon.getIssuedAt()),
        toLocalDateTime(issuedCoupon.getExpiredAt()));
  }

  @Transactional(readOnly = true)
  public CouponStockResult getStock(Long couponId) {
    Coupon coupon = couponRepository.readActiveCoupon(couponId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다."));
    return new CouponStockResult(couponId, coupon.getRemainingQuantity());
  }

  @Transactional(readOnly = true)
  public CouponUserCouponsResult getUserCoupons(Long userId) {
    List<CouponUserCouponsResult.CouponItem> coupons = issuedCouponRepository.readUserCoupons(userId).stream()
        .map(issuedCoupon -> new CouponUserCouponsResult.CouponItem(
            issuedCoupon.getId(),
            issuedCoupon.getCoupon().getId(),
            issuedCoupon.getCoupon().getName(),
            issuedCoupon.getStatus(),
            toLocalDateTime(issuedCoupon.getIssuedAt()),
            toLocalDateTime(issuedCoupon.getExpiredAt()),
            toLocalDateTime(issuedCoupon.getUsedAt())))
        .toList();
    return new CouponUserCouponsResult(coupons);
  }

  @Transactional(readOnly = true)
  public CouponUserCouponsResult getUsableCoupons(Long userId) {
    ZonedDateTime now = ZonedDateTime.now(clock);
    List<CouponUserCouponsResult.CouponItem> coupons = issuedCouponRepository.readUsableUserCoupons(userId, now)
        .stream()
        .filter(issuedCoupon -> issuedCoupon.isUsableAt(now))
        .map(issuedCoupon -> new CouponUserCouponsResult.CouponItem(
            issuedCoupon.getId(),
            issuedCoupon.getCoupon().getId(),
            issuedCoupon.getCoupon().getName(),
            issuedCoupon.getStatus(),
            toLocalDateTime(issuedCoupon.getIssuedAt()),
            toLocalDateTime(issuedCoupon.getExpiredAt()),
            toLocalDateTime(issuedCoupon.getUsedAt())))
        .toList();
    return new CouponUserCouponsResult(coupons);
  }

  @Transactional
  public CouponUseResult useCoupon(CouponUseCommand command) {
    ZonedDateTime now = ZonedDateTime.now(clock);
    IssuedCoupon issuedCoupon = issuedCouponRepository.readOwnedCoupon(command.issuedCouponId(), command.userId())
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용 가능한 쿠폰이 존재하지 않습니다."));

    if (!issuedCoupon.isUsableAt(now)) {
      throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용되었거나 만료된 쿠폰입니다.");
    }

    Coupon coupon = issuedCoupon.getCoupon();
    int orderAmount = validateOrderAmount(command.orderAmount());
    validateMinimumOrderAmount(coupon, command.orderAmount());
    int discountAmount = calculateDiscountAmount(coupon, orderAmount);

    issuedCoupon.use(now);

    return new CouponUseResult(
        issuedCoupon.getId(),
        command.orderId(),
        discountAmount,
        issuedCoupon.getStatus(),
        toLocalDateTime(issuedCoupon.getUsedAt()));
  }

  private int validateOrderAmount(Integer orderAmount) {
    if (orderAmount == null || orderAmount <= 0) {
      throw new CoreException(ErrorType.BAD_REQUEST, "주문 금액이 올바르지 않습니다.");
    }
    return orderAmount;
  }

  private void validateMinimumOrderAmount(Coupon coupon, int orderAmount) {
    Integer minOrderAmount = coupon.getMinOrderAmount();
    if (minOrderAmount != null && orderAmount < minOrderAmount) {
      throw new CoreException(ErrorType.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다.");
    }
  }

  private int calculateDiscountAmount(Coupon coupon, int orderAmount) {
    int discountAmount = 0;
    if (coupon.getCouponType() == CouponType.FIXED_AMOUNT) {
      discountAmount = Math.min(coupon.getDiscountValue(), orderAmount);
    }
    if (coupon.getCouponType() == CouponType.PERCENTAGE) {
      discountAmount = orderAmount * coupon.getDiscountValue() / 100;
    }

    Integer maxDiscountAmount = coupon.getMaxDiscountAmount();
    if (maxDiscountAmount != null) {
      discountAmount = Math.min(discountAmount, maxDiscountAmount);
    }
    return discountAmount;
  }

  private LocalDateTime toLocalDateTime(ZonedDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.toLocalDateTime();
  }
}
