package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
import com.dragons.constant.Constants.LockKey;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public abstract class PaymentStrategy<C extends PaymentCommand, R extends PaymentResult> {
  private final SubscriptionRepository subscriptionRepository;
  private final PaymentRepository paymentRepository;
  private final DistributedLockFactory distributedLockFactory;
  private final Clock clock;

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
  public void subscribe(String name, String email, String planType, String status) {
    String lockKey = LockKey.LOCK_SUBSCRIBE + email;
    distributedLockFactory
        .get(LockType.REDIS)
        .executeWithLock(
            lockKey,
            LockOptions.of(Duration.ofSeconds(5)),  // DB 조회/저장 시간 고려
            () -> {
              subscribeInternal(name, email, planType, status);
              return null;  // Void 작업이므로 null 반환
            }
        )
        .orElseThrow(() -> new CoreException(
            ErrorType.CONFLICT,
            "다른 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."
        ));
  }

  private void subscribeInternal(String name, String email, String planType, String status) {
    boolean exists = subscriptionRepository.existsByEmail(email);

    if (exists) {
      throw new CoreException(ErrorType.CONFLICT, "현재 구독중인 회원입니다.");
    }

    Optional<Subscription> expire = subscriptionRepository.findExpiredByEmail(email);

    if (expire.isEmpty()) {
      log.info("신규 등록");
      subscriptionRepository.save(Subscription.apply(clock, email, name, planType, status));
      return;
    }

    Subscription subscription = expire.get();
    subscription.renew(clock);
    subscriptionRepository.save(subscription);
  }

  public Payment pay(final String name, final String email, final Long amount, final String planType,
      final String paymentType) {
    return paymentRepository.save(Payment.use(name,
        email,
        amount,
        planType,
        paymentType));
  }

  public Payment pay(final String orderId, final String name, final String email, final Long amount,
      final String planType,
      final String paymentType) {
    return paymentRepository.save(Payment.createOrder(
        orderId,
        name,
        email,
        amount,
        planType,
        paymentType));
  }

}
