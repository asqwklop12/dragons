package com.dragons.infra.toss;

import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.payment.TossPaymentConfirmation;
import com.dragons.exception.NonRetryableException;
import com.dragons.exception.RetryableException;
import com.dragons.executor.RetryExecutor;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class PgPaymentClientImpl implements PgPaymentClient {

  private final RestTemplate restTemplate;
  private final PgProperties pgProperties;

  private final RetryExecutor retryExecutor = new RetryExecutor();

  public PgPaymentClientImpl(RestTemplate restTemplate, PgProperties pgProperties) {
    this.restTemplate = restTemplate;
    this.pgProperties = pgProperties;
  }

  @Override
  public TossPaymentConfirmation confirm(String paymentKey, String orderId, long amount) {
    return retryExecutor.execute(() -> {
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
        return restTemplate.postForObject(url, entity, TossPaymentConfirmation.class);
      } catch (HttpStatusCodeException e) {
        log.error("Toss 결제 확인 실패: paymentKey={}, orderId={}", paymentKey, orderId, e);
        if (e.getStatusCode().is4xxClientError()) {
          throw new NonRetryableException(e); // 재시도 의미 없음
        }
        throw new RetryableException(e);
      } catch (ResourceAccessException e) {
        // 타임아웃, 커넥션 문제
        throw new RetryableException(e);
      }
    });

  }

}
