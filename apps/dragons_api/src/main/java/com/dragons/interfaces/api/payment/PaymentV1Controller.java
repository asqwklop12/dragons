package com.dragons.interfaces.api.payment;

import com.dragons.application.payment.PaymentService;
import com.dragons.application.payment.dto.PaymentCardCommand;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto.Bank;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto.Card;
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
      @Validated @RequestBody PaymentV1Dto.Card.Request request) {
    var command = new PaymentCardCommand(
        request.cardNumber(),
        request.expiryMonth(),
        request.expiryYear(),
        request.cvc(),
        request.cardholderName(),
        request.amount(),
        request.planType());
    var result = paymentService.card(command);

    return ApiResponse.success(new Card.Response(
        result.cardNumber(),
        result.amount(),
        result.planType()));
  }

  // 계좌이체
  @Override
  @PostMapping("/bank-transfer")
  public ApiResponse<PaymentV1Dto.Bank.Response> bankTransfer(
      @Validated @RequestBody PaymentV1Dto.Bank.Request request) {
    return ApiResponse.success(new Bank.Response(
        "1234567890",
        "홍길동",
        100,
        "premium"));
  }
}
