package com.dragons.infra.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import com.dragons.domain.payment.TossPaymentClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TossPaymentClientImpl implements TossPaymentClient {

    private final RestTemplate restTemplate;
    private final TossProperties tossProperties;

    public TossPaymentClientImpl(RestTemplate restTemplate, TossProperties tossProperties) {
        this.restTemplate = restTemplate;
        this.tossProperties = tossProperties;
    }

    @Override
    public Map<String, Object> confirm(String paymentKey, String orderId, long amount) {
        String url = tossProperties.getBaseUrl() + "/payments/confirm";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodeSecretKey(tossProperties.getSecretKey()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        return restTemplate.postForObject(url, entity, Map.class);
    }

    private String encodeSecretKey(String secretKey) {
        String key = secretKey + ":";
        return Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }
}
