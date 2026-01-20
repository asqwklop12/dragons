package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class PaymentStrategy<C extends PaymentCommand, R extends PaymentResult> {
  private final SubscriptionRepository subscriptionRepository;
  private final PaymentRepository paymentRepository;

  public abstract PaymentType supports();

  public abstract R pay(C command);

  public void success(String paymentKey, String orderId, Long amount) {

  }

  public void fail(String code, String message, String orderId) {
  }

  protected PaymentRepository getPaymentRepository() {
    return paymentRepository;
  }

  // 구독은 하나로 통합
  public void subscribe(String name, String planType, String status) {
    if (!subscriptionRepository.exists(name)) {
      // 카드로 구독 신청이 완료 상태로 들어간다.
      subscriptionRepository.save(
          Subscription.apply(
              name,
              planType,
              status));
    }
  }

  public Payment pay(final String name, final Long amount, final String planType, final String paymentType) {
    return paymentRepository.save(Payment.use(name,
        amount,
        planType,
        paymentType));
  }

  public Payment pay(final String orderId, final String name, final Long amount, final String planType,
                     final String paymentType) {
    return paymentRepository.save(Payment.createOrder(
        orderId,
        name,
        amount,
        planType,
        paymentType));
  }


}
