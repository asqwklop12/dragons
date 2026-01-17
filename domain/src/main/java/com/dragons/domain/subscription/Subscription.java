package com.dragons.domain.subscription;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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

  public static Subscription apply(String holderName, String planType, String status) {
    if (holderName == null || holderName.isBlank()) {
      throw new IllegalArgumentException("holderName은 필수입니다");
    }
    return new Subscription(holderName, PlanType.valueOf(planType), Status.valueOf(status));
  }

  public Subscription(String holderName, PlanType planType, Status status) {
    this.holderName = holderName;
    this.planType = planType;
    this.status = status;
  }

  public enum PlanType {
    PREMIUM // 현재는 premium버전만 존재
  }

  public enum Status {
    ACTIVE, WAITING, CANCELLED, EXPIRED
  }

}
