package com.dragons.application.payment.dto;

public sealed interface PaymentCommand
    permits PaymentCardCommand,
    PaymentBankTransferCommand,
    PaymentPgCommand { }
