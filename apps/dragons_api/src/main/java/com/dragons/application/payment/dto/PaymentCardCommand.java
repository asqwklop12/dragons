package com.dragons.application.payment.dto;

public record PaymentCardCommand(
    String cardNumber,
    int expiryMonth,
    int expiryYear,
    String cvc,
    String cardholderName,
    long amount,
    String planType
) {
}
