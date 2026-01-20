package com.dragons.application.payment.dto;

public record PaymentPgResult(
    String orderId,
    long amount,
    String orderName,
    String customerName,
    String planType
) implements PaymentResult {
}
