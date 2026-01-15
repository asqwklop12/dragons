package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.application.payment.dto.PaymentTossCommand;
import com.dragons.application.payment.dto.PaymentTossResult;
import com.dragons.domain.payment.Card;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository repository;
  private final SubscriptionRepository subscriptionRepository;
  private final PgPaymentClient pgPaymentClient;

  public PaymentTossResult toss(PaymentTossCommand command) {
    String orderId = UUID.randomUUID().toString();
    Payment payment = Payment.createOrder(
        orderId,
        command.customerName(),
        command.amount(),
        command.planType(),
        "TOSS");
    repository.save(payment);

    return new PaymentTossResult(
        orderId,
        command.amount(),
        command.orderName(),
        command.customerName(),
        command.planType());
  }

  public PaymentCardResult card(PaymentCardCommand command) {
    Payment payment = repository.save(Payment.use(command.cardholderName(),
        command.amount(),
        command.planType(),
        "card"));
    String maskingCardNumber = Card.masking(command.cardNumber());

    if (!subscriptionRepository.exists(payment.holderName())) {
      // 카드로 구독 신청이 완료 상태로 들어간다.
      subscriptionRepository.save(
          Subscription.apply(
              payment.holderName(),
              PlanType.PREMIUM.name(),
              Status.ACTIVE.name()));
    }

    return new PaymentCardResult(maskingCardNumber, payment.holderName(), payment.amount(), payment.planType());
  }

  public PaymentBankTransferResult bankTransfer(PaymentBankTransferCommand command) {
    Payment payment = repository.save(Payment.use(command.depositorName(),
        command.amount(),
        command.planType(),
        "bank"));

    // 계좌이체로 구독 신청이 대기 상태로 들어간다.
    if (!subscriptionRepository.exists(payment.holderName())) {
      subscriptionRepository.save(
          Subscription.apply(
              payment.holderName(),
              PlanType.PREMIUM.name(),
              Status.WAITING.name()));

    }
    return new PaymentBankTransferResult(command.bankCode(), command.accountNumber(), payment.holderName(),
        payment.amount(), payment.planType());
  }

  @Transactional
  public void confirmTossPayment(String paymentKey, String orderId, long amount) {
    // 승인처리
    pgPaymentClient.confirm(paymentKey, orderId, amount);

    // 결제 상태 업데이트
    Payment payment = repository.findByOrderId(orderId)
        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "Payment not found for orderId: " + orderId));
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

  public void failTossPayment(String code, String message, String orderId) {

    // 구독 취소 처리를 한다.
    log.warn("[실패 사유] {}", message);
  }
}
