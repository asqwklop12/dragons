package com.dragons.domain.subscription;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
  @Column(nullable = false, unique = true)
  private String holderName;

  @Enumerated(EnumType.STRING)
  private PlanType planType;

  @Enumerated(EnumType.STRING)
  private Status status;


  @Column
  private ZonedDateTime expireDate;

  public static Subscription apply(final Clock clock, String holderName, String planType, String status) {
    if (holderName == null || holderName.isBlank()) {
      throw new IllegalArgumentException("holderName은 필수입니다");
    }
    return new Subscription(clock, holderName, PlanType.valueOf(planType.toUpperCase()), Status.valueOf(status));
  }

  public Subscription(final Clock clock, String holderName, PlanType planType, Status status) {
    this.holderName = holderName;
    this.planType = planType;
    this.status = status;
    this.expireDate = ZonedDateTime.now(clock).plusMonths(1); // 만료일은 1개월 후로 설정
  }

  public void renew(final Clock clock) {
    if (status == Status.WAITING) {
      throw new IllegalArgumentException("대기 상태인 구독은 활성화가 불가능합니다.");
    }
    ZonedDateTime now = ZonedDateTime.now(clock);
    ZonedDateTime base = (expireDate != null && expireDate.isAfter(now)) ? expireDate : now;
    this.expireDate = base.plusMonths(1);
    this.status = Status.ACTIVE;
  }

  public void cancel() {
    if (status == Status.EXPIRED) {
      throw new IllegalArgumentException("만료된 구독은 취소가 불가능합니다.");
    }
    if (status == Status.CANCELLED) {
      throw new IllegalArgumentException("이미 취소된 구독입니다.");
    }

    if (status == Status.WAITING) {
      throw new IllegalArgumentException("대기 상태인 구독은 취소가 불가능합니다.");
    }

    this.status = Status.CANCELLED;
  }

  public enum PlanType {
    PREMIUM // 현재는 premium버전만 존재
  }

  public enum Status {
    ACTIVE, WAITING, CANCELLED, EXPIRED
  }

}
