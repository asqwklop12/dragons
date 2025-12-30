package com.dragons.interfaces.api.payment.dto;

import jakarta.validation.constraints.*;

public class PaymentV1Dto {

  public static class Card {

    public record Request(
        // 16 digits
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "카드 번호는 16자리를 입력해야 합니다.")
        String cardNumber,

        // month 1-12
        @Min(1)
        @Max(12)
        int expiryMonth,

        // 4-digit year
        @Min(2000)
        @Max(9999)
        int expiryYear,

        // keep as String to allow leading zeros
        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "cvc번호는 3자리여야 합니다.")
        String cvc,

        @NotBlank
        String cardholderName,

        // amount must be > 0
        @Positive(message = "금액은 양수여야합니다.")
        int amount,

        @NotBlank
        String planType

    ) {
    }

    public record Response(
        String cardNumber, // 카드 번호
        int amount, // 금액
        String planType // 결제타입
    ) {

    }


  }

  public static class Bank {

    public record Request(
        // 은행코드: 3자리 숫자
        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "은행 코드는 3자리 숫자입니다.")
        String bankCode,
        @NotBlank
        @Pattern(regexp = "\\d{10,14}", message = "계좌번호는 10~14자리 숫자입니다.")
        String accountNumber,
        @NotBlank
        @Size(min = 2, max = 20, message = "예금주명은 2~20자리입니다.")
        String depositorName,
        @Positive(message = "금액은 양수여야합니다.")
        int amount,
        @NotBlank
        String planType
    ) {

    }

    public record Response(
        String accountNumber, //계좌번호
        String depositorName, //예금주 명
        int amount, //금액
        String planType //결제타입

    ) {

    }
  }
}
