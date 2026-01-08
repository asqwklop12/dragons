package com.dragons.support.login;

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
    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
    HttpSession session = request.getSession(false); // 세션이 없으면 새로 만들지 않음

    if (session == null) {
      return null;
    }

    return session.getAttribute("userEmail");
  }
}
