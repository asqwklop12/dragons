package com.dragons.application.payment.dto;

public record PaymentBankTransferCommand(
    String bankCode,
    String accountNumber,
    String depositorName,
    int amount,
    String planType
) {
}
