package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.payment.Card;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import java.time.Clock;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentStrategy extends PaymentStrategy<PaymentCardCommand, PaymentCardResult> {

  public CardPaymentStrategy(Clock clock,
                             SubscriptionRepository subscriptionRepository,
                             DistributedLockFactory distributedLockFactory,
                             PaymentRepository paymentRepository) {
    super(subscriptionRepository, paymentRepository, distributedLockFactory, clock);
  }

  @Override
  public PaymentType supports() {
    return PaymentType.CARD;
  }

  @Override
  public PaymentCardResult pay(PaymentCardCommand command) {
    Payment payment = super.pay(command.cardholderName(), command.email(), command.amount(), command.planType(),
        "card");
    String maskingCardNumber = Card.masking(command.cardNumber());

    super.subscribe(payment.holderName(), payment.email(), payment.planType(), Status.ACTIVE.name());

    return new PaymentCardResult(maskingCardNumber, payment.holderName(), payment.amount(), payment.planType());
  }
}
