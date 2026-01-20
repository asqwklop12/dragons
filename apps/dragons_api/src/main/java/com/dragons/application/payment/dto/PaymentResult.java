package com.dragons.application.payment.dto;

public sealed interface PaymentResult
    permits PaymentCardResult,
    PaymentBankTransferResult,
    PaymentPgResult {
}
