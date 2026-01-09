package com.dragons.support.login;

import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
  private static final String SESSION_USER_EMAIL = "userEmail";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    // @LoginUser 어노테이션이 붙어 있고, 타입이 String(이메일)인지 체크
    return parameter.hasParameterAnnotation(LoginUser.class)
        && parameter.getParameterType().equals(String.class);
  }

  @Override
  public @Nullable Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory)
      throws Exception {
    // NativeWebRequest를 HttpServletRequest로 변환
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    if (request == null) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    HttpSession session = request.getSession(false);

    Object emailAttr = session.getAttribute(SESSION_USER_EMAIL);

    if (!(emailAttr instanceof String email) || email.isBlank()) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    return email;
  }
}
