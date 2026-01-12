package com.dragons.support.filter;


import com.dragons.support.util.SensitiveDataMasker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${logging.request-response.enabled:true}")
  private boolean loggingEnabled;

  @Value("${logging.request-response.body-enabled:true}")
  private boolean bodyLoggingEnabled;

  @Value("${logging.request-response.max-body-size:1024}")
  private int maxBodySize;

  private final Environment environment;


  private static final String PRETTY_LOG =
      """
          
          ╔══════════════════════════════════════════════════════════════
          ║ 🌐 HTTP Request/Response
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

  private static final int REQUEST_BUFFER_SIZE = 1024 * 1024; // 1MB

  private static final Set<String> BODY_LOGGING_EXCLUDE_PREFIX = Set.of(
      "/actuator",
      "/health",
      "/swagger-ui",
      "/v3/api-docs"
  );

  private boolean isProdProfile;

  @PostConstruct
  void init() {
    isProdProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
  }

  private boolean isBinaryRequest(HttpServletRequest request) {
    String ct = request.getContentType();
    return ct != null && (ct.contains("multipart") || ct.contains("octet-stream"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    if (!loggingEnabled) {
      filterChain.doFilter(request, response);
      return;
    }

    // 바이너리 요청은 래핑 없이 처리
    if (isBinaryRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    long start = System.currentTimeMillis();

    ContentCachingRequestWrapper wrappedRequest =
        request instanceof ContentCachingRequestWrapper
            ? (ContentCachingRequestWrapper) request
            : new ContentCachingRequestWrapper(request, REQUEST_BUFFER_SIZE);
    ContentCachingResponseWrapper wrappedResponse =
        response instanceof ContentCachingResponseWrapper
            ? (ContentCachingResponseWrapper) response
            : new ContentCachingResponseWrapper(response);

    Exception filterException = null;
    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } catch (Exception e) {
      filterException = e;
      throw e;
    }

    finally {
      long elapsed = System.currentTimeMillis() - start;

      if (!shouldSkipBodyLogging(wrappedRequest, wrappedResponse)) {
        logRequestResponse(wrappedRequest, wrappedResponse, elapsed);
      }
      if (filterException != null) {
        log.error("Request failed with exception", filterException);
      }

      wrappedResponse.copyBodyToResponse();
    }
  }

  private void logRequestResponse(ContentCachingRequestWrapper request,
                                  ContentCachingResponseWrapper response,
                                  long elapsed) {
    if (isProdProfile) {
      logCompact(request, response, elapsed);
    } else {
      // 로컬: 가독성 좋은 포맷
      logPretty(request, response, elapsed);
    }


  }

  private void logPretty(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long elapsed) {
    byte[] requestContent = request.getContentAsByteArray();
    byte[] responseContent = response.getContentAsByteArray();
    String requestBody = "";
    String responseBody = "";

    if (bodyLoggingEnabled) {
      requestBody = truncate(SensitiveDataMasker.maskSensitiveData(new String(requestContent, StandardCharsets.UTF_8)), maxBodySize);
      responseBody = truncate(SensitiveDataMasker.maskSensitiveData(new String(responseContent, StandardCharsets.UTF_8)), maxBodySize);
    }
    log.info(PRETTY_LOG,
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        elapsed,
        MDC.get("request_id"),
        bodyLoggingEnabled ? prettifyJson(requestBody) : "(body logging disabled)",
        bodyLoggingEnabled ? prettifyJson(responseBody) : "(body logging disabled)");

  }


  private void logCompact(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long elapsed) {
    byte[] requestContent = request.getContentAsByteArray();
    byte[] responseContent = response.getContentAsByteArray();

    log.info("REQ_RES method={} uri={} status={} time={}ms size_req={} size_res={}",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        elapsed,
        requestContent.length,
        responseContent.length
    );

    // Body는 조건부로만 로깅 (에러 상태 또는 설정된 경우만)
    if (bodyLoggingEnabled && (response.getStatus() >= 400 || log.isDebugEnabled())) {
      String requestBody = truncate(
          SensitiveDataMasker.maskSensitiveData(new String(requestContent, StandardCharsets.UTF_8)), maxBodySize);
      String responseBody = truncate(
          SensitiveDataMasker.maskSensitiveData(new String(responseContent, StandardCharsets.UTF_8)), maxBodySize);

      log.info("REQ_BODY={} RES_BODY={}", requestBody, responseBody);
    }
  }

  private String prettifyJson(String json) {
    if (json == null || json.isBlank()) {
      return "  (empty)";
    }

    try {
      Object jsonObject = objectMapper.readValue(json, Object.class);
      return "\n" + objectMapper
          .writerWithDefaultPrettyPrinter()
          .writeValueAsString(jsonObject)
          .lines()
          .map(line -> "║   " + line)  // 각 줄 앞에 인덴트 추가
          .collect(Collectors.joining("\n"));
    } catch (JsonProcessingException e) {
      // JSON이 아니면 그대로 반환
      return "║   " + json;
    }
  }

  private boolean shouldSkipBodyLogging(HttpServletRequest request, HttpServletResponse response) {
    String ct = request.getContentType();
    String responseCt = response.getContentType();
    String uri = request.getRequestURI();

    boolean uriExcluded = BODY_LOGGING_EXCLUDE_PREFIX.stream().anyMatch(uri::startsWith);
    boolean requestBinary = ct != null && (ct.contains("multipart") || ct.contains("octet-stream"));
    boolean responseBinary =
        responseCt != null && (responseCt.contains("image") || responseCt.contains("octet-stream"));

    return uriExcluded || requestBinary || responseBinary;

  }

  private String truncate(String str, int maxLength) {
    if (str == null || str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength) + "... (truncated)";
  }
}
