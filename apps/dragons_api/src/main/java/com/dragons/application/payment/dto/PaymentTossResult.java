package com.dragons.application.payment.dto;

public record PaymentTossResult(
    String orderId,
    long amount,
    String orderName,
    String customerName,
    String planType
) {
}
