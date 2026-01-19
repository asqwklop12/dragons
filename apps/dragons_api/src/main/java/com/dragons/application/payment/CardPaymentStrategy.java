package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
import com.dragons.domain.payment.Card;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy<PaymentCardCommand, PaymentCardResult> {
  private final PaymentRepository repository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  public PaymentType supports() {
    return PaymentType.CARD;
  }

  @Override
  public PaymentCardResult pay(PaymentCardCommand command) {
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
}
