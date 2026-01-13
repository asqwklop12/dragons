package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.domain.payment.Card;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository repository;
  private final SubscriptionRepository subscriptionRepository;

  public PaymentCardResult card(PaymentCardCommand command) {
    Payment payment = repository.save(Payment.use(command.cardholderName(),
        command.amount(),
        command.planType(),
        "card"));
    String maskingCardNumber = Card.masking(command.cardNumber());

    if (!subscriptionRepository.find(payment.holderName())) {
      // 카드로 구독 신청이 완료 상태로 들어간다.
      subscriptionRepository.apply(
          Subscription.aply(
              payment.holderName(),
              PlanType.PREMIUM.name(),
              Status.ACTIVE.name()
          )
      );
    }

    return new PaymentCardResult(maskingCardNumber, payment.holderName(), payment.amount(), payment.planType());
  }

  public PaymentBankTransferResult bankTransfer(PaymentBankTransferCommand command) {
    Payment payment = repository.save(Payment.use(command.depositorName(),
        command.amount(),
        command.planType(),
        "bank"));

    // 계좌이체로 구독 신청이 대기 상태로 들어간다.
    if (!subscriptionRepository.find(payment.holderName())) {
      subscriptionRepository.apply(
          Subscription.aply(
              payment.holderName(),
              PlanType.PREMIUM.name(),
              Status.WAITING.name()
          )
      );

    }
    return new PaymentBankTransferResult(command.bankCode(), command.accountNumber(), payment.holderName(),
        payment.amount(), payment.planType());
  }
}
