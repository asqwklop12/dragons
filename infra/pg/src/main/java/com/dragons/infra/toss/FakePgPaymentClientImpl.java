package com.dragons.infra.toss;

import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.payment.TossPaymentConfirmation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local"})
public class FakePgPaymentClientImpl implements PgPaymentClient {
  @Override
  public TossPaymentConfirmation confirm(String paymentKey, String orderId, long amount) {
    return new TossPaymentConfirmation(paymentKey, orderId, amount);
  }
}
