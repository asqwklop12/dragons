package com.dragons.interfaces.api.subscription;

import com.dragons.application.subscription.SubscriptionService;
import com.dragons.interfaces.api.ApiResponse;
import com.dragons.interfaces.api.subscription.dto.SubscriptionV1Dto.Get.Response;
import com.dragons.support.login.LoginUser;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionV1Controller implements SubscriptionV1Spec {

  private final SubscriptionService subscriptionService;

  @Override
  @GetMapping
  public ApiResponse<Response> get(
      @Parameter(hidden = true) @LoginUser String email
  ) {
    var result = subscriptionService.get(email);
    return ApiResponse.success(new Response(
        result.holderName(),
        result.email(),
        result.planType(),
        result.status(),
        result.expireDate()));
  }

  @Override
  @PostMapping("/cancel")
  public ApiResponse<Void> cancel(@Parameter(hidden = true) @LoginUser String email) {
    subscriptionService.cancel(email);
    return ApiResponse.success(null);
  }
}
