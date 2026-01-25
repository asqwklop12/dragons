package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;

public interface PaymentService {
  PaymentResult request(PaymentType type, PaymentCommand command);

  void success(String paymentKey, String orderId, Long amount);

  void fail(String code, String message, String orderId);
}
