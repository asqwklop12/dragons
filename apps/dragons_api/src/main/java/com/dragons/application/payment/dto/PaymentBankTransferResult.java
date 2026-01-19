package com.dragons.application.payment.dto;

public record PaymentBankTransferResult(
    String bankCode,
    String accountNumber,
    String depositorName,
    long amount,
    String planType
) implements PaymentResult {

}
