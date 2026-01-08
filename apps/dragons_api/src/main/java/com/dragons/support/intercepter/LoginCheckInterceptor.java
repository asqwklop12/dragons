package com.dragons.support.intercepter;

import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

  private final String USER_EMAIL = "userEmail";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute(USER_EMAIL) != null) {
      // 컨트롤러에서 꺼내 쓸 수 있도록 request에 담아둠
      request.setAttribute(USER_EMAIL, session.getAttribute(USER_EMAIL));
      return true;
    }
    throw new CoreException(ErrorType.UNAUTHORIZED, "로그인이 필요합니다.");
  }

}
