package com.dragons.domain.payment;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(name = "payment_type", nullable = false)
  private String paymentType;

  @Column(nullable = false)
  private int amount;

  @Column(name = "plan_type", nullable = false)
  private String planType;

  @Column(name = "holder_name", nullable = false)
  private String holderName;

  protected Payment() {
  }

  public static Payment use(String holderName, int amount, String planType, String paymentType) {
    Payment payment = new Payment();
    payment.holderName = holderName;
    payment.amount = amount;
    payment.planType = planType;
    payment.paymentType = paymentType;
    return payment;
  }

  public static Payment withId(Long id, String holderName, int amount, String planType, String paymentType) {
    Payment payment = new Payment();
    payment.setIdForTest(id);
    payment.holderName = holderName;
    payment.amount = amount;
    payment.planType = planType;
    payment.paymentType = paymentType;
    return payment;
  }

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

  private void setIdForTest(Long id) {
    try {
      java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(this, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
