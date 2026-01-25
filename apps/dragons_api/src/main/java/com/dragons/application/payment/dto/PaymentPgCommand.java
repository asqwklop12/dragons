package com.dragons.application.payment.dto;

public record PaymentPgCommand(
        long amount,
        String orderName,
        String customerName,
        String email,
        String planType) implements PaymentCommand {
}
