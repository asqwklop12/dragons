package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private final PaymentStrategyResolver resolver;

  @Override
  @SuppressWarnings("unchecked")
  public PaymentResult request(PaymentType type, PaymentCommand command) {
    PaymentStrategy<PaymentCommand, PaymentResult> strategy =
        (PaymentStrategy<PaymentCommand, PaymentResult>) resolver.resolve(type);
    return strategy.pay(command);
  }

  @Override
  public void success(String paymentKey, String orderId, long amount) {
    resolver.confirm().success(paymentKey, orderId, amount);
  }

  @Override
  public void fail(String code, String message, String orderId) {
    resolver.confirm().fail(code, message, orderId);
  }


}
