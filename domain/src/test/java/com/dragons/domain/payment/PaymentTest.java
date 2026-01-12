package com.dragons.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    @DisplayName("결제 생성 성공")
    void use_success() {
        Payment payment = Payment.use("홍길동", 10000, "premium", "CARD");

        assertThat(payment.holderName()).isEqualTo("홍길동");
        assertThat(payment.amount()).isEqualTo(10000);
        assertThat(payment.planType()).isEqualTo("premium");
        assertThat(payment.paymentType()).isEqualTo("CARD");
        assertThat(payment.getId()).isNull();
        assertThat(payment.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("결제 취소(삭제) 및 복구")
    void delete_restore() {
        Payment payment = Payment.use("홍길동", 10000, "premium", "CARD");

        payment.delete();
        assertThat(payment.getDeletedAt()).isNotNull();

        payment.restore();
        assertThat(payment.getDeletedAt()).isNull();
    }
}
