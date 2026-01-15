package com.dragons.infra.jpa.payment;

import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class PaymentRepositoryImpl implements PaymentRepository {
    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        return jpaPaymentRepository.save(payment);
    }

    @Override
    public java.util.Optional<Payment> findByOrderId(String orderId) {
        return jpaPaymentRepository.findByOrderId(orderId);
    }
}
