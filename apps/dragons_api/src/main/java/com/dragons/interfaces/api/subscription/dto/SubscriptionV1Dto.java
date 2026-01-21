package com.dragons.interfaces.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;

public class SubscriptionV1Dto {

  public static class Cancel {

    public record Request(@NotBlank(message = "이름은 필수입니다.")
                          String name) {
    }
  }
}
