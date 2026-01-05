package com.dragons.infra.jpa.payment;

import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = new PaymentEntity(
                payment.holderName(),
                payment.amount(),
                payment.planType(),
                payment.paymentType());
        return toDomain(jpaPaymentRepository.save(entity));
    }

    private Payment toDomain(PaymentEntity entity) {
        return Payment.withId(
                entity.getId(),
                entity.getHolderName(),
                entity.getAmount(),
                entity.getPlanType(),
                entity.getPaymentType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
