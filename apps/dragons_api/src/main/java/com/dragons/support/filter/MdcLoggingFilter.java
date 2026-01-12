package com.dragons.support.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "request_id";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    private static final Set<String> BODY_LOGGING_EXCLUDE_PREFIX = Set.of(
        "/actuator",
        "/health",
        "/swagger-ui",
        "/v3/api-docs",
        "/api/auth"
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


        String requestId = request.getHeader(HEADER_REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put(REQUEST_ID, requestId);
        wrappedResponse.setHeader(HEADER_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long elapsed = System.currentTimeMillis() - start;

            byte[] reqBody = wrappedRequest.getContentAsByteArray();
            byte[] resBody = wrappedResponse.getContentAsByteArray();

            String requestBody = new String(reqBody, StandardCharsets.UTF_8);
            String responseBody = new String(resBody, StandardCharsets.UTF_8);

            wrappedResponse.copyBodyToResponse();

            boolean skip = shouldSkipBodyLogging(request);

            if(!skip) {
                log.info("METHOD={} URI={} STATUS={} TIME={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsed);
                log.info("RequestBody = {}", requestBody);
                log.info("ResponseBody = {}", responseBody);
            }

            MDC.clear();
        }
    }

    private boolean shouldSkipBodyLogging(HttpServletRequest request) {
        String ct = request.getContentType();
        String uri = request.getRequestURI();
        return BODY_LOGGING_EXCLUDE_PREFIX.stream().anyMatch(uri::startsWith) ||
            ct != null && (ct.contains("multipart") || ct.contains("octet-stream"));
    }
}
