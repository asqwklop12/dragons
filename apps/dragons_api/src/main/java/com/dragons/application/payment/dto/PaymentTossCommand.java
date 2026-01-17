package com.dragons.application.payment.dto;

public record PaymentTossCommand(
    long amount,
    String orderName,
    String customerName,
    String planType
) {
}
