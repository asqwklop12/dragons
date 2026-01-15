package com.dragons.application.payment.dto;

public record PaymentTossCommand(
    int amount,
    String orderName,
    String customerName,
    String planType
) {
}
