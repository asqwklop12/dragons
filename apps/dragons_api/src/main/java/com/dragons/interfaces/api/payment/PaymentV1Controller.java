package com.dragons.interfaces.api.payment;

import com.dragons.application.payment.PaymentService;
import com.dragons.application.payment.PaymentType;
import com.dragons.application.payment.dto.PaymentBankTransferCommand;
import com.dragons.application.payment.dto.PaymentBankTransferResult;
import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.application.payment.dto.PaymentCardResult;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto.Bank;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto.Card;
import com.dragons.support.login.LoginUser;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentV1Controller implements PaymentV1Spec {
  private final PaymentService paymentService;

  // 카드
  @Override
  @PostMapping("/card")
  public ApiResponse<PaymentV1Dto.Card.Response> card(
      @Parameter(hidden = true)  @LoginUser String email,
      @Validated @RequestBody PaymentV1Dto.Card.Request request) {
    var command = new PaymentCardCommand(
        request.cardNumber(),
        request.expiryMonth(),
        request.expiryYear(),
        request.cvc(),
        request.cardholderName(),
        email,
        request.amount(),
        request.planType());
    var response = (PaymentCardResult) paymentService.request(PaymentType.CARD, command);

    return ApiResponse.success(new Card.Response(
        response.cardNumber(),
        response.amount(),
        response.planType()));
  }

  // 계좌이체
  @Override
  @PostMapping("/bank-transfer")
  public ApiResponse<PaymentV1Dto.Bank.Response> bankTransfer(
      @Parameter(hidden = true)  @LoginUser String email,
      @Validated @RequestBody PaymentV1Dto.Bank.Request request) {

    var command = new PaymentBankTransferCommand(
        request.bankCode(),
        request.accountNumber(),
        request.depositorName(),
        email,
        request.amount(),
        request.planType());
    var response = (PaymentBankTransferResult) paymentService.request(PaymentType.BANK_TRANSFER, command);

    return ApiResponse.success(new Bank.Response(
        response.bankCode(),
        response.accountNumber(),
        response.depositorName(),
        response.amount(),
        response.planType()));
  }
}
