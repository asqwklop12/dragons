package com.dragons.interfaces.api.payment;

import com.dragons.application.payment.PaymentService;
import com.dragons.application.payment.PaymentType;
import com.dragons.application.payment.dto.PaymentPgResult;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto;
import com.dragons.support.login.LoginUser;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/payments/toss")
@RequiredArgsConstructor
public class TossPaymentV1Controller implements TossPaymentV1Spec {

  private final PaymentService paymentService;

  @Override
  @PostMapping
  public PaymentV1Dto.Toss.Response request(
      @Parameter(hidden = true) @LoginUser String email,
      @RequestBody @Validated PaymentV1Dto.Toss.Request request) {

    var result = (PaymentPgResult) paymentService.request(PaymentType.PG, request.toCommand(email));

    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
        .build().toUriString();

    return new PaymentV1Dto.Toss.Response(
        result.orderId(),
        result.amount(),
        result.orderName(),
        result.customerName(),
        baseUrl + "/api/payments/toss/success",
        baseUrl + "/api/payments/toss/fail");
  }

  @Override
  @GetMapping("/success")
  public ApiResponse<Void> success(
      @RequestParam String paymentKey,
      @RequestParam String orderId,
      @RequestParam Long amount) {
    paymentService.success(paymentKey, orderId, amount);
    return ApiResponse.success(null);
  }

  @Override
  @GetMapping("/fail")
  public ApiResponse<Void> fail(
      @RequestParam String code,
      @RequestParam String message,
      @RequestParam String orderId) {
    paymentService.fail(code, message, orderId);
    return ApiResponse.success(null);
  }

}
