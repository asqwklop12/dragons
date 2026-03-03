package com.dragons.domain.coupon;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CouponType couponType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CouponStatus status;

  @Column(nullable = false)
  private Integer discountValue;

  @Column
  private Integer minOrderAmount;

  @Column
  private Integer maxDiscountAmount;

  @Column(nullable = false)
  private Integer totalQuantity;

  @Column(nullable = false)
  private Integer issuedQuantity;

  @Column(nullable = false)
  private Integer validDays;

  @Column(nullable = false)
  private ZonedDateTime startDate;

  @Column(nullable = false)
  private ZonedDateTime endDate;

  protected Coupon() {
  }

  public boolean isIssuableAt(ZonedDateTime now) {
    return status == CouponStatus.ACTIVE
        && (startDate.isBefore(now) || startDate.isEqual(now))
        && (endDate.isAfter(now) || endDate.isEqual(now))
        && getRemainingQuantity() > 0;
  }

  public int getRemainingQuantity() {
    return Math.max(totalQuantity - issuedQuantity, 0);
  }

  public void issue() {
    if (getRemainingQuantity() <= 0) {
      throw new IllegalStateException("쿠폰 발급 수량이 모두 소진되었습니다.");
    }

    this.issuedQuantity += 1;
    if (getRemainingQuantity() == 0) {
      this.status = CouponStatus.EXHAUSTED;
    }
  }
}
