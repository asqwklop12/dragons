package com.dragons.interfaces.api.subscription;

import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.subscription.dto.SubscriptionV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Subscription", description = "구독 관리 API")
public interface SubscriptionV1Spec {

  @Operation(summary = "구독 조회 (본인)", description = "로그인한 사용자의 구독을 조회합니다.")
  ApiResponse<SubscriptionV1Dto.Get.Response> get(String email);

  @Operation(summary = "구독 취소 (본인)", description = "로그인한 사용자의 구독을 취소합니다.")
  ApiResponse<Void> cancel(String email);
}
