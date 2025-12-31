package com.dragons.application.payment;

import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.domain.payment.Card;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final PaymentRepository repository;

  public PaymentCardResult card(PaymentCardCommand command) {
    Payment payment = repository.save(Payment.use(command.cardholderName(),
                                                  command.amount(),
                                                  command.planType(),
                                      "card"));
    String maskingCardNumber = Card.masking(command.cardNumber());
    return new PaymentCardResult(maskingCardNumber, payment.holderName(), payment.amount(), payment.planType());
  }

  public PaymentBankTransferResult bankTransfer(PaymentBankTransferCommand command) {
    Payment payment = repository.save(Payment.use(command.depositorName(),
                                                  command.amount(),
                                                  command.planType(),
                                      "bank"));



    return new PaymentBankTransferResult(command.bankCode(), command.accountNumber(), payment.holderName(), payment.amount(), payment.planType());
  }
}
