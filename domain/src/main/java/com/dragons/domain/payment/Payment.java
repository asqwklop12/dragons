package com.dragons.domain.payment;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(name = "payment_type", nullable = false)
  private String paymentType;

  @Column(nullable = false)
  private long amount;

  @Column(name = "plan_type", nullable = false)
  private String planType;

  @Column(name = "holder_name", nullable = false)
  private String holderName;

  @Column(name = "email")
  private String email;

  @Column(name = "payment_key")
  private String paymentKey;

  @Column(name = "order_id", nullable = false, unique = true)
  private String orderId;

  protected Payment() {
  }

  public static Payment use(String holderName, String email, long amount, String planType, String paymentType) {
    Payment payment = new Payment();
    payment.orderId = UUID.randomUUID().toString();
    payment.holderName = holderName;
    payment.email = email;
    payment.amount = amount;
    payment.planType = planType;
    payment.paymentType = paymentType;
    return payment;
  }

  public static Payment createOrder(String orderId, String holderName, String email, long amount, String planType,
      String paymentType) {
    Payment payment = new Payment();
    payment.orderId = orderId;
    payment.holderName = holderName;
    payment.email = email;
    payment.amount = amount;
    payment.planType = planType;
    payment.paymentType = paymentType;
    return payment;
  }

  public static Payment withId(Long id, String holderName, String email, long amount, String planType,
      String paymentType) {
    Payment payment = new Payment();
    payment.setIdForTest(id);
    payment.holderName = holderName;
    payment.email = email;
    payment.orderId = UUID.randomUUID().toString();
    payment.amount = amount;
    payment.planType = planType;
    payment.paymentType = paymentType;
    return payment;
  }

  public String paymentType() {
    return paymentType;
  }

  public String paymentKey() {
    return paymentKey;
  }

  public String orderId() {
    return orderId;
  }

  public long amount() {
    return amount;
  }

  public String planType() {
    return planType;
  }

  public String holderName() {
    return holderName;
  }

  public String email() {
    return email;
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

  public void updateKey(String paymentKey) {
    this.paymentKey = paymentKey;
  }
}
