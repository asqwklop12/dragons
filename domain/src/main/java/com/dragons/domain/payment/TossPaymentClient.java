package com.dragons.domain.payment;

import java.util.Map;

public interface TossPaymentClient {
    Map<String, Object> confirm(String paymentKey, String orderId, long amount);
}
