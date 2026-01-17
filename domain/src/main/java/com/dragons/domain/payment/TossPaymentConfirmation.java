package com.dragons.domain.payment;

public record TossPaymentConfirmation(
    String paymentKey,
    String orderId,
    long amount
) {
}
