package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentPgCommand;
import com.dragons.application.payment.dto.PaymentPgResult;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PgPaymentStrategy extends PaymentStrategy<PaymentPgCommand, PaymentPgResult> {
  private final PgPaymentClient pgPaymentClient;
  private final PaymentRepository repository;


  public PgPaymentStrategy(SubscriptionRepository subscriptionRepository, PaymentRepository paymentRepository,
                           PgPaymentClient pgPaymentClient, PaymentRepository repository) {
    super(subscriptionRepository, paymentRepository);
    this.pgPaymentClient = pgPaymentClient;
    this.repository = repository;
  }

  @Override
  public PaymentType supports() {
    return PaymentType.PG;
  }

  @Override
  public PaymentPgResult pay(PaymentPgCommand command) {
    String orderId = UUID.randomUUID().toString();
    Payment payment = super.pay(orderId, command.customerName(), command.amount(), command.planType(),"TOSS");

    return new PaymentPgResult(
        orderId,
        command.amount(),
        command.orderName(),
        command.customerName(),
        payment.planType());
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
    super.subscribe(payment.holderName(), PlanType.PREMIUM.name(), Status.ACTIVE.name());
  }

  @Override
  public void fail(String code, String message, String orderId) {

    // 구독 취소 처리를 한다.
    log.warn("[실패 사유] {}", message);
  }

}
