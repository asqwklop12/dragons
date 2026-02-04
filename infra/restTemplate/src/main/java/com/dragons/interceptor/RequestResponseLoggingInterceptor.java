package com.dragons.interceptor;

import com.dragons.constant.LogColor;
import com.dragons.masking.MaskingFacade;
import com.dragons.util.SensitiveDataMasker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

@Slf4j
@RequiredArgsConstructor
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final MaskingFacade maskingFacade;
  private final boolean isProd;



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
      {}
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
          LogColor.YELLOW,
          request.getMethod(),
          request.getURI(),
          status,
          elapsed,
          MDC.get("extra_request_id"),
          truncate(prettifyJson(maskedRequestBody), MAX_BODY_SIZE),
          truncate(prettifyJson(maskedResponseBody), MAX_BODY_SIZE),
          LogColor.RESET);
    }
  }

  private String prettifyJson(String data) {
    if (data == null || data.isBlank()) {
      return "(empty)";
    }

    // JSON 파싱 시도
    try {
      Object obj = objectMapper.readValue(data, Object.class);
      String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
      return addBoxPrefix(prettyJson);
    } catch (JsonProcessingException e) {
      // JSON이 아닌 경우 URL-encoded 형식인지 확인
      if (data.contains("=") && data.contains("&")) {
        return prettifyFormData(data);
      }
      return "\n║   " + data;
    }
  }

  private String prettifyFormData(String formData) {
    try {
      String[] pairs = formData.split("&");
      StringBuilder formatted = new StringBuilder();
      formatted.append("\n");
      for (int i = 0; i < pairs.length; i++) {
        if (i > 0) {
          formatted.append("\n");
        }
        formatted.append("║   ").append(pairs[i]);
      }
      return formatted.toString();
    } catch (Exception e) {
      return "\n║   " + formData;
    }
  }

  private String addBoxPrefix(String content) {
    if (content == null || content.isBlank()) {
      return content;
    }

    String[] lines = content.split("\n");
    StringBuilder result = new StringBuilder();
    result.append("\n");
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        result.append("\n");
      }
      result.append("║   ").append(lines[i]);
    }

    return result.toString();
  }


  private String truncate(String str, int maxLength) {
    if (str == null || str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength) + "... (truncated)";
  }
}
