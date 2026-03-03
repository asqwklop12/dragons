package com.dragons.domain.coupon;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
@Entity
@Table(name = "issued_coupons")
public class IssuedCoupon extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "coupon_id", nullable = false)
  private Coupon coupon;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private IssuedCouponStatus status;

  @Column(nullable = false)
  private ZonedDateTime issuedAt;

  @Column(nullable = false)
  private ZonedDateTime expiredAt;

  @Column
  private ZonedDateTime usedAt;

  protected IssuedCoupon() {
  }

  public static IssuedCoupon issue(Coupon coupon, Long userId, ZonedDateTime issuedAt) {
    IssuedCoupon issuedCoupon = new IssuedCoupon();
    issuedCoupon.coupon = coupon;
    issuedCoupon.userId = userId;
    issuedCoupon.status = IssuedCouponStatus.ISSUED;
    issuedCoupon.issuedAt = issuedAt;
    issuedCoupon.expiredAt = issuedAt.plusDays(coupon.getValidDays());
    issuedCoupon.usedAt = null;
    return issuedCoupon;
  }

  public boolean isUsableAt(ZonedDateTime now) {
    return status == IssuedCouponStatus.ISSUED
        && (expiredAt.isAfter(now) || expiredAt.isEqual(now));
  }

  public void use(ZonedDateTime usedAt) {
    if (status != IssuedCouponStatus.ISSUED) {
      throw new IllegalStateException("이미 사용되었거나 사용할 수 없는 쿠폰입니다.");
    }
    if (usedAt.isBefore(issuedAt)) {
      throw new IllegalStateException("발급 시각 이전에는 쿠폰을 사용할 수 없습니다.");
    }
    if (usedAt.isAfter(expiredAt)) {
      throw new IllegalStateException("만료된 쿠폰은 사용할 수 없습니다.");
    }
    this.status = IssuedCouponStatus.USED;
    this.usedAt = usedAt;
  }
}
