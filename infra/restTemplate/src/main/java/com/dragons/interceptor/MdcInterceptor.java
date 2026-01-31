package com.dragons.interceptor;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class MdcInterceptor implements ClientHttpRequestInterceptor {
  private static final String REQUEST_ID = "extra_request_id";
  private static final String HEADER_REQUEST_ID = "X_Extra_Request_Id";

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    String requestId = request.getHeaders().getFirst(HEADER_REQUEST_ID);

    boolean isBlankRequestId = requestId == null || requestId.isBlank();
    if (isBlankRequestId) {
      requestId = "E-" + UUID.randomUUID();
    }

    // 1. Request Header에 추가 (외부 호출용)
    if (isBlankRequestId) {
      request.getHeaders().add(HEADER_REQUEST_ID, requestId);
    }

    // 2. MDC 세팅 (로그용)
    String previous = MDC.get(REQUEST_ID);
    MDC.put(REQUEST_ID, requestId);

    try {
      // 3. 실제 HTTP 호출
      return execution.execute(request, body);
    } finally {
      if (previous == null) {
        MDC.remove(REQUEST_ID);
      } else {
        MDC.put(REQUEST_ID, previous);
      }
    }


  }


}
