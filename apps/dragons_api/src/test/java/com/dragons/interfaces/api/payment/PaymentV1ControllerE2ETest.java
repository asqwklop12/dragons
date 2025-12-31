package com.dragons.interfaces.api.payment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dragons.utils.DragonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@DragonIntegrationTest
class PaymentV1ControllerE2ETest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @DisplayName("카드 결제 - 정상 케이스")
  void card_success() throws Exception {
    // given
    String requestBody = """
        {
          "cardNumber": "1234123412341234",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "cvc": "123",
          "cardholderName": "홍길동",
          "amount": 10000,
          "planType": "premium"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/payments/card")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        // ApiResponse 래핑 반영: 본문은 $.data 하위에 위치
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.cardNumber").value("123456******5678"))
        .andExpect(jsonPath("$.data.amount").value(100))
        .andExpect(jsonPath("$.data.planType").value("premium"));
  }

  @Test
  @DisplayName("카드 결제 - 카드번호 길이 오류")
  void card_fail_invalidCardNumber() throws Exception {
    // given
    String requestBody = """
        {
          "cardNumber": "1234",
          "expiryMonth": 12,
          "expiryYear": 2025,
          "cvc": "123",
          "cardholderName": "홍길동",
          "amount": 10000,
          "planType": "premium"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/payments/card")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("카드 결제 - 유효기간 월 오류")
  void card_fail_invalidExpiryMonth() throws Exception {
    // given
    String requestBody = """
        {
          "cardNumber": "1234123412341234",
          "expiryMonth": 13,
          "expiryYear": 2025,
          "cvc": "123",
          "cardholderName": "홍길동",
          "amount": 10000,
          "planType": "premium"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/payments/card")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("계좌이체 - 정상 케이스")
  void bankTransfer_success() throws Exception {
    // given
    String requestBody = """
        {
          "bankCode": "004",
          "accountNumber": "123456789012",
          "depositorName": "홍길동",
          "amount": 10000,
          "planType": "premium"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/payments/bank-transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.accountNumber").value("1234567890"))
        .andExpect(jsonPath("$.data.depositorName").value("홍길동"))
        .andExpect(jsonPath("$.data.amount").value(100))
        .andExpect(jsonPath("$.data.planType").value("premium"));
  }

  @Test
  @DisplayName("계좌이체 - 은행코드 오류")
  void bankTransfer_fail_invalidBankCode() throws Exception {
    // given
    String requestBody = """
        {
          "bankCode": "12",
          "accountNumber": "123456789012",
          "depositorName": "홍길동",
          "amount": 10000,
          "planType": "premium"
        }
        """;

    // when & then
    mockMvc.perform(post("/api/payments/bank-transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }
}
