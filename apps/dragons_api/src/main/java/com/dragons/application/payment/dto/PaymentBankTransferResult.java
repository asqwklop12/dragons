package com.dragons.application.payment.dto;

public record PaymentBankTransferResult(
    String bankCode,
    String accountNumber,
    String depositorName,
    int amount,
    String planType
) {

}
