package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.application.payment.dto.PaymentCommand;
import com.dragons.application.payment.dto.PaymentResult;
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
public class BankTransferPaymentStrategy implements PaymentStrategy<PaymentBankTransferCommand,PaymentBankTransferResult> {
  private final PaymentRepository repository;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  public PaymentType supports() {
    return PaymentType.BANK_TRANSFER;
  }

  @Override
  public PaymentBankTransferResult pay(PaymentBankTransferCommand command) {
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

    return new PaymentBankTransferResult(command.bankCode(), command.accountNumber(),
        payment.holderName(),
        payment.amount(), payment.planType());
  }
}
