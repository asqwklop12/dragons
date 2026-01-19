package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;

public interface PaymentStrategy {
  PaymentType supports();
  PaymentResult pay(PaymentCommand command);

  default void success(String paymentKey, String orderId, long amount) {}
  default void fail(String code, String message, String orderId) {}
}
