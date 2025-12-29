package com.dragons.dragons_api.interfaces.payment;

import com.dragons.dragons_api.interfaces.payment.dto.PaymentV1Dto;
import com.dragons.dragons_api.interfaces.payment.dto.PaymentV1Dto.Bank;
import com.dragons.dragons_api.interfaces.payment.dto.PaymentV1Dto.Card;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentV1Controller {
  // 카드
  @PostMapping("/card")
  public PaymentV1Dto.Card.Response card(@Validated @RequestBody PaymentV1Dto.Card.Request request) {
    return new Card.Response(
        "123456******5678",
        100,
        "premium"
    );
  }

  // 계좌이체
  @PostMapping("/bank-transfer")
  public PaymentV1Dto.Bank.Response bankTransfer(@Validated @RequestBody PaymentV1Dto.Bank.Request request) {
    return new Bank.Response(
        "1234567890",
        "홍길동",
        100,
        "premium"
    );
  }

}
