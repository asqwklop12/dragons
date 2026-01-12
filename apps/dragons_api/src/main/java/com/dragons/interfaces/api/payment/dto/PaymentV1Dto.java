package com.dragons.interfaces.api.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.LuhnCheck;

@Schema(name = "PaymentV1Dto", description = "결제 API v1 DTO 집합")
public class PaymentV1Dto {

  @Schema(name = "PaymentCard", description = "카드 결제 DTO")
  public static class Card {

    @Schema(name = "PaymentCardRequest", description = "카드 결제 요청")
    public record Request(
        // 16 digits
        @Schema(description = "카드 번호(16자리)", example = "1234123412341234")
        @NotBlank
        @Pattern(regexp = "\\d{16}", message = "카드 번호는 16자리를 입력해야 합니다.")
        @LuhnCheck(message = "카드 번호가 유효하지 않습니다.")
        String cardNumber,

        // month 1-12
        @Schema(description = "만료 월(1~12)", example = "12")
        @Min(1)
        @Max(12)
        int expiryMonth,

        // 4-digit year
        @Schema(description = "만료 연도(4자리)", example = "2025")
        @Min(2000)
        @Max(9999)
        int expiryYear,

        // keep as String to allow leading zeros
        @Schema(description = "CVC(3자리)", example = "123")
        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "cvc번호는 3자리여야 합니다.")
        String cvc,

        @Schema(description = "소유자명", example = "홍길동")
        @NotBlank
        String cardholderName,

        // amount must be > 0
        @Schema(description = "결제 금액(양수)", example = "10000")
        @Positive(message = "금액은 양수여야합니다.")
        int amount,

        @Schema(description = "요금제/플랜 타입", example = "premium")
        @NotBlank
        String planType

    ) {
      public Request {
        if(planType == null) {
          planType = "premium"; //임시 플랜이 정해지면 제거
        }
      }
    }

    @Schema(name = "PaymentCardResponse", description = "카드 결제 응답")
    public record Response(
        @Schema(description = "마스킹된 카드 번호", example = "123456******5678")
        String cardNumber, // 카드 번호
        @Schema(description = "결제 금액", example = "100")
        int amount, // 금액
        @Schema(description = "요금제/플랜 타입", example = "premium")
        String planType // 결제타입
    ) {

    }


  }

  @Schema(name = "PaymentBank", description = "계좌이체 결제 DTO")
  public static class Bank {

    @Schema(name = "PaymentBankRequest", description = "계좌이체 결제 요청")
    public record Request(
        // 은행코드: 3자리 숫자
        @Schema(description = "은행 코드(3자리)", example = "004")
        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "은행 코드는 3자리 숫자입니다.")
        String bankCode,
        @Schema(description = "계좌번호(10~14자리)", example = "123456789012")
        @NotBlank
        @Pattern(regexp = "\\d{10,14}", message = "계좌번호는 10~14자리 숫자입니다.")
        String accountNumber,
        @Schema(description = "예금주명(2~20자)", example = "홍길동")
        @NotBlank
        @Size(min = 2, max = 20, message = "예금주명은 2~20자리입니다.")
        String depositorName,
        @Schema(description = "결제 금액(양수)", example = "10000")
        @Positive(message = "금액은 양수여야합니다.")
        int amount,
        @Schema(description = "요금제/플랜 타입", example = "premium")
        @NotBlank
        String planType
    ) {

    }

    @Schema(name = "PaymentBankResponse", description = "계좌이체 결제 응답")
    public record Response(
        // 은행코드: 3자리 숫자
        @Schema(description = "은행 코드(3자리)", example = "004")
        String bankCode,
        @Schema(description = "마스킹된 계좌번호", example = "1234567890")
        String accountNumber, //계좌번호
        @Schema(description = "예금주명", example = "홍길동")
        String depositorName, //예금주 명
        @Schema(description = "결제 금액", example = "100")
        int amount, //금액
        @Schema(description = "요금제/플랜 타입", example = "premium")
        String planType //결제타입

    ) {

    }
  }
}
