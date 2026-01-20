package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.subscription.Subscription.PlanType;
import com.dragons.domain.subscription.Subscription.Status;
import com.dragons.domain.subscription.SubscriptionRepository;
import org.springframework.stereotype.Component;

@Component
public class BankTransferPaymentStrategy extends
    PaymentStrategy<PaymentBankTransferCommand, PaymentBankTransferResult> {

  public BankTransferPaymentStrategy(SubscriptionRepository subscriptionRepository,
                                     PaymentRepository paymentRepository) {
    super(subscriptionRepository, paymentRepository);
  }

  @Override
  public PaymentType supports() {
    return PaymentType.BANK_TRANSFER;
  }

  @Override
  public PaymentBankTransferResult pay(PaymentBankTransferCommand command) {
    Payment payment = super.pay(command.depositorName(), command.amount(), command.planType(), "bank");
    subscribe(payment.holderName(), PlanType.PREMIUM.name(), Status.WAITING.name());

    return new PaymentBankTransferResult(command.bankCode(), command.accountNumber(),
        payment.holderName(),
        payment.amount(), payment.planType());
  }
}
