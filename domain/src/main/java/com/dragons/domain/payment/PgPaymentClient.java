package com.dragons.domain.payment;

public interface PgPaymentClient {
    TossPaymentConfirmation confirm(String paymentKey, String orderId, long amount);
}
