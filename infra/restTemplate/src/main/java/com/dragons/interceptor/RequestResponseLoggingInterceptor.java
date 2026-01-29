package com.dragons.interceptor;

import com.dragons.cononstant.LogColor;
import com.dragons.util.SensitiveDataMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final boolean isProd;

  public RequestResponseLoggingInterceptor(boolean isProd) {
    this.isProd = isProd;
  }

  public RequestResponseLoggingInterceptor() {
    this(false);
  }

  private static final int MAX_BODY_SIZE = 1024;

  private static final String PRETTY_LOG = """
      {}
      ╔══════════════════════════════════════════════════════════════
      ║ 🌍 OUTBOUND HTTP
      ╠══════════════════════════════════════════════════════════════
      ║ Method    : {}
      ║ URI       : {}
      ║ Status    : {}
      ║ Duration  : {}ms
      ║ Request ID: {}
      ╠══════════════════════════════════════════════════════════════
      ║ 📤 Request Body:
      ╠══════════════════════════════════════════════════════════════
      ║   {}
      ╠══════════════════════════════════════════════════════════════
      ║ 📥 Response Body:
      ╠══════════════════════════════════════════════════════════════
      ║   {}
      ╚══════════════════════════════════════════════════════════════
      """;

  private static final String FLAT_LOG = "🌍 OUTBOUND HTTP Method: {}, URI: {}, Status: {}, Duration: {}ms, Request ID: {}, Request Body: {}, Response Body: {}";

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    long start = System.currentTimeMillis();
    ClientHttpResponse response = null;
    try {
      response = execution.execute(request, body);
      return response;
    } finally {
      long elapsed = System.currentTimeMillis() - start;
      logRequestResponse(request, body, response, elapsed);
    }
  }

  private void logRequestResponse(
      HttpRequest request,
      byte[] requestBodyBytes,
      ClientHttpResponse response,
      long elapsed) {

    String maskedRequestBody = SensitiveDataMasker.maskSensitiveData(
        new String(requestBodyBytes, StandardCharsets.UTF_8));

    String maskedResponseBody = "";
    int status = -1;

    try {
      if (response != null) {
        status = response.getStatusCode().value();
        byte[] responseBytes = response.getBody().readAllBytes();
        maskedResponseBody = SensitiveDataMasker.maskSensitiveData(
            new String(responseBytes, StandardCharsets.UTF_8));
      }
    } catch (Exception e) {
      log.warn("Failed to read response body", e);
    }

    if (isProd) {
      log.info(FLAT_LOG,
          request.getMethod(),
          request.getURI(),
          status,
          elapsed,
          MDC.get("extra_request_id"),
          truncate(maskedRequestBody, MAX_BODY_SIZE),
          truncate(maskedResponseBody, MAX_BODY_SIZE));
    } else {
      log.info(PRETTY_LOG,
          LogColor.PURPLE,
          request.getMethod(),
          request.getURI(),
          status,
          elapsed,
          MDC.get("extra_request_id"),
          truncate(prettifyJson(maskedRequestBody), MAX_BODY_SIZE),
          truncate(prettifyJson(maskedResponseBody), MAX_BODY_SIZE));
    }
  }

  private String prettifyJson(String json) {
    if (json == null || json.isBlank()) {
      return "(empty)";
    }

    try {
      Object obj = objectMapper.readValue(json, Object.class);
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      return json; // JSON 아니면 그대로
    }
  }

  private String truncate(String str, int maxLength) {
    if (str == null || str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength) + "... (truncated)";
  }
}
