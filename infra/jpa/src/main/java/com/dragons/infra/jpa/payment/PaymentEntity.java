package com.dragons.infra.jpa.payment;

import com.dragons.infra.jpa.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
@Getter
public class PaymentEntity extends BaseEntity {

    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "plan_type", nullable = false, length = 50)
    private String planType;

    @Column(name = "holder_name", nullable = false, length = 100)
    private String holderName;

    public PaymentEntity(String holderName, int amount, String planType, String paymentType) {
        this.holderName = holderName;
        this.amount = amount;
        this.planType = planType;
        this.paymentType = paymentType;
    }
}
