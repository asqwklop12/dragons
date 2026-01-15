package com.dragons.application.payment.dto;

public record PaymentCardResult(
    String cardNumber,
    String cardHolderName,
    long amount,
    String planType
) {
}
