package com.dragons.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
  Payment save(Payment payment);

  Payment saveAndFlush(Payment payment);

  Optional<Payment> findByOrderId(String orderId);
}
