package com.dragons.infra.toss;

import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.payment.TossPaymentConfirmation;
import com.dragons.executor.RetryExecutor;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Profile({"!local"})
@Slf4j
@Component
@RequiredArgsConstructor
public class PgPaymentClientImpl implements PgPaymentClient {

  private final RestTemplate restTemplate;
  private final PgProperties pgProperties;

  private final RetryExecutor retryExecutor;


  @Override
  public TossPaymentConfirmation confirm(String paymentKey, String orderId, long amount) {
    String url = pgProperties.getBaseUrl() + "/payments/confirm";

    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(pgProperties.getSecretKey(), "");
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    Map<String, Object> requestBody = Map.of(
        "paymentKey", paymentKey,
        "orderId", orderId,
        "amount", amount);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    return retryExecutor.execute(() -> restTemplate.postForObject(url, entity, TossPaymentConfirmation.class));

  }
}
