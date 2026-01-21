package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class PaymentStrategy<C extends PaymentCommand, R extends PaymentResult> {
  private final SubscriptionRepository subscriptionRepository;
  private final PaymentRepository paymentRepository;

  public abstract PaymentType supports();

  public abstract R pay(C command);

  /**
   * 결제 성공 후처리 훅 (기본 no-op).
   */
  public void success(String paymentKey, String orderId, Long amount) {

  }

  /**
   * 결제 성공 후처리 훅 (기본 no-op).
   */
  public void fail(String code, String message, String orderId) {
  }

  protected PaymentRepository getPaymentRepository() {
    return paymentRepository;
  }

  // 구독은 하나로 통합
  @Transactional
  public void subscribe(String name, String planType, String status) {

    boolean exists = subscriptionRepository.exists(name);

    if (exists) {
      throw new CoreException(ErrorType.CONFLICT, "현재 구독중인 회원입니다.");
    }

    Optional<Subscription> expire = subscriptionRepository.findExpiredByHolderName(name);

    if (expire.isEmpty()) {
      log.info("신규 등록");
      subscriptionRepository.save(Subscription.apply(name, planType, status));
      return;
    }

    Subscription subscription = expire.get();
    subscription.renew();
    subscriptionRepository.save(subscription);
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
