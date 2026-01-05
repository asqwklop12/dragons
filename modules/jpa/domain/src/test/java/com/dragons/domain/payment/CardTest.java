package com.dragons.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    @DisplayName("카드 번호 마스킹: 앞 6자리, 뒤 4자리를 제외하고 마스킹된다")
    void masking() {
        // given
        String cardNumber = "1234567890123456";

        // when
        String masked = Card.masking(cardNumber);

        // then
        assertThat(masked).isEqualTo("123456******3456");
    }

}
