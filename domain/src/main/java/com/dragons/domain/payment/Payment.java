package com.dragons.domain.payment;

import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class Payment {
  private Long id;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime deletedAt;

  private String paymentType;
  private int amount;
  private String planType;
  private String holderName;

  public static Payment use(String holderName, int amount, String planType, String paymentType) {
    return new Payment(null, holderName, amount, planType, paymentType, null, null, null);
  }

  public static Payment withId(Long id, String holderName, int amount, String planType, String paymentType,
      ZonedDateTime createdAt, ZonedDateTime updatedAt, ZonedDateTime deletedAt) {
    return new Payment(id, holderName, amount, planType, paymentType, createdAt, updatedAt, deletedAt);
  }

  private Payment(Long id, String holderName, int amount, String planType, String paymentType,
      ZonedDateTime createdAt, ZonedDateTime updatedAt, ZonedDateTime deletedAt) {
    this.id = id;
    this.holderName = holderName;
    this.amount = amount;
    this.planType = planType;
    this.paymentType = paymentType;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  // Manual fluent accessors to support previous API
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

  public void delete() {
    if (this.deletedAt == null) {
      this.deletedAt = ZonedDateTime.now();
    }
  }

  public void restore() {
    if (this.deletedAt != null) {
      this.deletedAt = null;
    }
  }
}
