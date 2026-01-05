package com.dragons.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    @DisplayName("카드 번호 마스킹 - 10자리 이상")
    void masking_long() {
        // 16 digits: 123456 789012 3456 -> 123456******3456
        String cardNumber = "1234567890123456";
        String masked = Card.masking(cardNumber);

        assertThat(masked).isEqualTo("123456******3456");
        assertThat(masked.length()).isEqualTo(cardNumber.length());
        assertThat(masked.substring(0, 6)).isEqualTo("123456");
        assertThat(masked.substring(masked.length() - 4)).isEqualTo("3456");
    }

    @Test
    @DisplayName("카드 번호 마스킹 - 10자리 미만")
    void masking_short() {
        String cardNumber = "123456789";
        String masked = Card.masking(cardNumber);

        assertThat(masked).isEqualTo("123456789");
    }
}
