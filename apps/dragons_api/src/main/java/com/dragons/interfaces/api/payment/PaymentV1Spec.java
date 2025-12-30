package com.dragons.interfaces.api.payment;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.payment.dto.PaymentV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Payment V1 API", description = "결제 처리 API")
public interface PaymentV1Spec {

  @Operation(
      summary = "카드 결제",
      description = "카드 정보를 이용해 결제를 수행합니다."
  )
  ApiResponse<PaymentV1Dto.Card.Response> card(PaymentV1Dto.Card.Request request);

  @Operation(
      summary = "계좌이체 결제",
      description = "계좌이체 정보를 이용해 결제를 수행합니다."
  )
  ApiResponse<PaymentV1Dto.Bank.Response> bankTransfer(PaymentV1Dto.Bank.Request request);
}
