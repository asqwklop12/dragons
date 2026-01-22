package com.dragons.interfaces.api.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.ZonedDateTime;

public class SubscriptionV1Dto {

  public static class Cancel {

    public record Request(@NotBlank(message = "이름은 필수입니다.")
                          String name) {
    }
  }


  public static class Get {

    public record Response(
        String holderName,
        String planType,
        String status,
        ZonedDateTime expireDate
    ) {
    }
  }
}
