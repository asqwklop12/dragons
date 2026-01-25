package com.dragons.application.payment.dto;

public record PaymentBankTransferCommand(
        String bankCode,
        String accountNumber,
        String depositorName,
        String email,
        long amount,
        String planType) implements PaymentCommand {
}
