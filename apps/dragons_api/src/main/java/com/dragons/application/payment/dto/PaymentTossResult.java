package com.dragons.application.payment.dto;

public record PaymentTossResult(
    String orderId,
    int amount,
    String orderName,
    String customerName,
    String planType
) {
}
