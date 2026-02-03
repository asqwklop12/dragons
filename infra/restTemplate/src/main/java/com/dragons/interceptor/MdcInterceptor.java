package com.dragons.interceptor;

import com.dragons.constant.Constants;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class MdcInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    String requestId = request.getHeaders().getFirst(Constants.HEADER_EXTRA_REQUEST_ID);

    boolean isBlankRequestId = requestId == null || requestId.isBlank();
    if (isBlankRequestId) {
      requestId = "E-" + UUID.randomUUID();
    }

    // 1. Request Header에 추가 (외부 호출용)
    if (isBlankRequestId) {
      request.getHeaders().add(Constants.HEADER_EXTRA_REQUEST_ID, requestId);
    }

    // 2. MDC 세팅 (로그용)
    String previous = MDC.get(Constants.EXTRA_REQUEST_ID);
    MDC.put(Constants.EXTRA_REQUEST_ID, requestId);

    try {
      // 3. 실제 HTTP 호출
      return execution.execute(request, body);
    } finally {
      if (previous == null) {
        MDC.remove(Constants.EXTRA_REQUEST_ID);
      } else {
        MDC.put(Constants.EXTRA_REQUEST_ID, previous);
      }
    }


  }


}
