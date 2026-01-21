package com.dragons.application.subscription;

import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void cancel(String holderName) {
        Subscription subscription = subscriptionRepository.findByHolderName(holderName)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "구독 정보를 찾을 수 없습니다."));

        subscription.cancel();

    }
}
