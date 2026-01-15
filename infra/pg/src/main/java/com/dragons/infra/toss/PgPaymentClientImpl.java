package com.dragons.infra.toss;

import com.dragons.domain.payment.PgPaymentClient;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PgPaymentClientImpl implements PgPaymentClient {

  private final RestTemplate restTemplate;
  private final PgProperties pgProperties;

  public PgPaymentClientImpl(RestTemplate restTemplate, PgProperties pgProperties) {
    this.restTemplate = restTemplate;
    this.pgProperties = pgProperties;
  }

  @Override
  public Map<String, Object> confirm(String paymentKey, String orderId, long amount) {
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

    try {
      return restTemplate.postForObject(url, entity, Map.class);
    } catch (RestClientException e) {
      log.error("Toss 결제 확인 실패: paymentKey={}, orderId={}", paymentKey, orderId, e);
      throw new RuntimeException("결제 확인 중 오류가 발생했습니다", e);
    }
  }

}
