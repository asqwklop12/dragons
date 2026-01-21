package com.dragons.interfaces.api.subscription;

import com.dragons.application.subscription.SubscriptionService;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.subscription.dto.SubscriptionV1Dto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionV1Controller implements SubscriptionV1Spec {

  private final SubscriptionService subscriptionService;

  @Override
 @PostMapping("/cancel")
  public ApiResponse<Void> cancel(@RequestBody @Valid SubscriptionV1Dto.Cancel.Request request) {
    subscriptionService.cancel(request.name());
    return ApiResponse.success(null);
  }
}
