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
}
