package com.dragons.infrastructure.payment;

import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends PaymentRepository, JpaRepository<Payment, Long> {
}
