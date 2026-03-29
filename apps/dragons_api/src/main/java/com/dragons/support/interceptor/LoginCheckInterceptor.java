package com.dragons.support.interceptor;

import com.dragons.domain.constants.SessionConstants;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

  static final String EMAIL = SessionConstants.SESSION_USER_EMAIL.getValue();
  static final String DEV_EMAIL_HEADER = "X-Dev-User-Email";

  private final Environment environment;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Object emailAttr = session.getAttribute(EMAIL);
      if (emailAttr instanceof String email && StringUtils.hasText(email)) {
        request.setAttribute(EMAIL, email);
        return true;
      }
    }

    if (environment.matchesProfiles( "dev")) {
      String devEmail = request.getHeader(DEV_EMAIL_HEADER);
      if (StringUtils.hasText(devEmail)) {
        String email = devEmail.trim();
        request.getSession(true).setAttribute(EMAIL, email);
        request.setAttribute(EMAIL, email);
        return true;
      }
    }

    throw new CoreException(ErrorType.UNAUTHORIZED, "로그인이 필요합니다.");
  }
}
