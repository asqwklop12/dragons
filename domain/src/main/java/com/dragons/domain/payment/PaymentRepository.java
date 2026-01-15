package com.dragons.domain.payment;

public interface PaymentRepository {
  Payment save(Payment payment);

  java.util.Optional<Payment> findByOrderId(String orderId);
}
