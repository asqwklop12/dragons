package com.dragons.interfaces.api.payment;

import com.dragons.interfaces.api.payment.dto.PaymentV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Toss Payment V1 API", description = "토스 결제 연동 API")
public interface TossPaymentV1Spec {

  @Operation(summary = "토스 결제 요청", description = "토스 결제 주문 생성을 요청합니다.")
  PaymentV1Dto.Toss.Response request(
      PaymentV1Dto.Toss.Request request);

  @Operation(summary = "토스 결제 성공 콜백", description = "토스 결제 성공 콜백을 처리합니다.")
  void success(String paymentKey, String orderId, Long amount);

  @Operation(summary = "토스 결제 실패 콜백", description = "토스 결제 실패 콜백을 처리합니다.")
  void fail(String code, String message, String orderId);
}
