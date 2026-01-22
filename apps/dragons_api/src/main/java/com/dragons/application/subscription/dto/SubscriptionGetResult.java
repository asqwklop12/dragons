package com.dragons.application.subscription.dto;

import java.time.ZonedDateTime;

public record SubscriptionGetResult(
    String holderName,
    String planType,
    String status,
    ZonedDateTime expireDate
) {
}
