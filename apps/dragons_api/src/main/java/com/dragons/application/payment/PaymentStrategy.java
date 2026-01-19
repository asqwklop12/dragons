package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;

public interface PaymentStrategy<C extends PaymentCommand, R extends PaymentResult> {
  PaymentType supports();

  R pay(C command);

  default void success(String paymentKey, String orderId, long amount) {
  }

  default void fail(String code, String message, String orderId) {
  }
}
