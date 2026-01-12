package com.dragons.support.filter;

import com.dragons.support.util.SensitiveDataMasker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
  private static final Set<String> BODY_LOGGING_EXCLUDE_PREFIX = Set.of(
      "/actuator",
      "/health",
      "/swagger-ui",
      "/v3/api-docs"
  );

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();

    ContentCachingRequestWrapper wrappedRequest =
        new ContentCachingRequestWrapper(request, 1024 * 1024);
    ContentCachingResponseWrapper wrappedResponse =
        new ContentCachingResponseWrapper(response);

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      long elapsed = System.currentTimeMillis() - start;

      // api 요청은 알려줬으면 좋겠다.
      log.info("METHOD={} URI={} STATUS={} TIME={}ms",
          wrappedRequest.getMethod(),
          wrappedRequest.getRequestURI(),
          wrappedResponse.getStatus(),
          elapsed);

      if (!shouldSkipBodyLogging(request, response)) {
        logRequestResponse(wrappedRequest, wrappedResponse);
      }

      wrappedResponse.copyBodyToResponse();
    }
  }

  private void logRequestResponse(ContentCachingRequestWrapper request,
                                  ContentCachingResponseWrapper response) {
    byte[] requestContent = request.getContentAsByteArray();
    byte[] responseContent = response.getContentAsByteArray();
    String requestBody = new String(requestContent, StandardCharsets.UTF_8);
    String responseBody = new String(responseContent, StandardCharsets.UTF_8);

    if (requestContent.length > 0) {
      String maskedRequest = SensitiveDataMasker.maskSensitiveData(requestBody);
      log.info("RequestBody = {}", truncate(maskedRequest, 100));
    }

    if (responseContent.length > 0) {
      String maskedResponse = SensitiveDataMasker.maskSensitiveData(responseBody);
      log.info("ResponseBody = {}", truncate(maskedResponse, 100));
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
