package com.dragons.interfaces.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Toss Payment V1 API", description = "토스 결제 연동 API")
public interface TossPaymentV1Spec {

  @Operation(summary = "토스 결제 요청", description = "토스 결제 창 요청을 수행합니다.")
  void request();

  @Operation(summary = "토스 결제 성공 콜백", description = "토스 결제 성공 콜백을 처리합니다.")
  void success();

  @Operation(summary = "토스 결제 실패 콜백", description = "토스 결제 실패 콜백을 처리합니다.")
  void fail();
}
