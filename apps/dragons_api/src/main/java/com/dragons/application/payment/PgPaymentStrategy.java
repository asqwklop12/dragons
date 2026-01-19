package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentPgCommand;
import com.dragons.application.payment.dto.PaymentPgResult;
import com.dragons.application.payment.dto.PaymentResult;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentStrategy implements PaymentStrategy {
  private final PaymentRepository repository;
  private final SubscriptionRepository subscriptionRepository;
  private final PgPaymentClient pgPaymentClient;

  @Override
  public PaymentType supports() {
    return PaymentType.PG;
  }

  @Override
  public PaymentResult pay(PaymentCommand command) {
    String orderId = UUID.randomUUID().toString();
    PaymentPgCommand pgCommand = (PaymentPgCommand) command;
    Payment payment = Payment.createOrder(
        orderId,
        pgCommand.customerName(),
        pgCommand.amount(),
        pgCommand.planType(),
        "TOSS");
    repository.save(payment);

    return new PaymentPgResult(
        orderId,
        pgCommand.amount(),
        pgCommand.orderName(),
        pgCommand.customerName(),
        pgCommand.planType());
  }

  @Override
  @Transactional
  public void success(String paymentKey, String orderId, long amount) {
    Payment payment = repository.findByOrderId(orderId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "Payment not found for orderId: " + orderId));

    // 금액 검증
    if (payment.amount() != amount) {
      throw new CoreException(ErrorType.BAD_REQUEST,
          "결제 금액 불일치: expected=" + payment.amount() + ", actual=" + amount);
    }

    // 승인처리
    pgPaymentClient.confirm(paymentKey, orderId, amount);

    // 결제 완료 처리
    payment.updateKey(paymentKey);

    // 구독 처리를 한다.
    if (!subscriptionRepository.exists(payment.holderName())) {
      subscriptionRepository.save(
          Subscription.apply(
              payment.holderName(),
              PlanType.PREMIUM.name(),
              Status.ACTIVE.name()));
    }
  }

  @Override
  public void fail(String code, String message, String orderId) {

    // 구독 취소 처리를 한다.
    log.warn("[실패 사유] {}", message);
  }

}
