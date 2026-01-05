package com.dragons.infra.jpa.payment;

import com.dragons.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
}
