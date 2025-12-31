package com.dragons.domain.payment;

import com.dragons.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(name = "payment_type", nullable = false, length = 50)
  private String paymentType;

  @Column(name = "amount", nullable = false)
  private int amount;

  @Column(name = "plan_type", nullable = false, length = 50)
  private String planType;

  @Column(name = "holder_name", nullable = false, length = 100)
  private String holderName;

  public String paymentType() {
    return paymentType;
  }

  public int amount() {
    return amount;
  }

  public String planType() {
    return planType;
  }

  public String holderName() {
    return holderName;
  }
}
