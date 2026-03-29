package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentPgCommand;
import com.dragons.application.payment.dto.PaymentPgResult;
import com.dragons.constant.Constants.Lock;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.payment.TossPaymentConfirmation;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PgPaymentStrategy extends PaymentStrategy<PaymentPgCommand, PaymentPgResult> {
  private final PgPaymentClient pgPaymentClient;
  private final PaymentRepository paymentRepository;
  private final DistributedLockFactory distributedLockFactory;

  public PgPaymentStrategy(
      Clock clock,
      SubscriptionRepository subscriptionRepository, PaymentRepository paymentRepository,
      DistributedLockFactory distributedLockFactory,
      PgPaymentClient pgPaymentClient) {
    super(subscriptionRepository, paymentRepository, distributedLockFactory, clock);
    this.paymentRepository = paymentRepository;
    this.pgPaymentClient = pgPaymentClient;
    this.distributedLockFactory = distributedLockFactory;
  }

  @Override
  public PaymentType supports() {
    return PaymentType.PG;
  }

  @Override
  public PaymentPgResult pay(PaymentPgCommand command) {
    String orderId = UUID.randomUUID().toString();
    Payment payment = super.pay(orderId, command.customerName(), command.email(), command.amount(), command.planType(),
        "TOSS");

    return new PaymentPgResult(
        orderId,
        command.amount(),
        command.orderName(),
        command.customerName(),
        payment.planType());
  }

  @Override
  @Transactional
  public void success(String paymentKey, String orderId, Long amount) {
    Payment payment = getPaymentRepository().findByOrderId(orderId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "Payment not found for orderId: " + orderId));

    if (payment.paymentKey() != null) {
      if (payment.paymentKey().equals(paymentKey)) {
        log.info("이미 동일한 paymentKey로 처리된 결제입니다. orderId={}", orderId);
        return;
      }

      throw new CoreException(ErrorType.CONFLICT, "이미 처리된 결제입니다.");
    }

    // 금액 검증
    if (payment.amount() != amount) {
      throw new CoreException(ErrorType.BAD_REQUEST,
          "결제 금액 불일치: expected=" + payment.amount() + ", actual=" + amount);
    }

    String lockKey = Lock.LOCK_PAYMENT_CONFIRM + orderId;

    // 승인처리
    Optional<TossPaymentConfirmation> result;
    try {
      result = distributedLockFactory.get(LockType.REDIS).executeWithLock(
          lockKey,
          LockOptions.of(Duration.ofSeconds(10)),
          () -> pgPaymentClient.confirm(paymentKey, orderId, amount)
      );
    } catch (LockException e) {
      log.error("분산락 처리 중 오류가 발생했습니다. orderId={}, lockKey={}", orderId, lockKey, e);
      throw new CoreException(ErrorType.LOCK_ERROR, "분산락 처리 중 오류가 발생했습니다.");
    }

    // 분산락을
    if (result.isEmpty()) {
      throw new CoreException(ErrorType.CONFLICT, "이미 처리중인 결제입니다.");
    }

    try {
      // 결제 완료 처리
      payment.updateKey(paymentKey);
      paymentRepository.saveAndFlush(payment);
    } catch (DataIntegrityViolationException e) {
      throw new CoreException(ErrorType.CONFLICT, "이미 처리된 PaymentKey입니다.");
    }

    // 구독 처리를 한다.
    super.subscribe(payment.holderName(), payment.email(), payment.planType(), Status.ACTIVE.name());
  }

  @Override
  public void fail(String code, String message, String orderId) {

    // 구독 취소 처리를 한다.
    log.warn("[실패 사유] {}", message);
  }

}
