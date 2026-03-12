package com.dragons.support.config;

import com.dragons.support.interceptor.LoginCheckInterceptor;
import com.dragons.support.login.LoginUserArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final LoginCheckInterceptor loginCheckInterceptor;
  private final LoginUserArgumentResolver loginUserArgumentResolver;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginCheckInterceptor)
        .order(1) // 제일 먼저 실행되도록 설정
        .addPathPatterns("/**") // 모든 경로를 일단 막음
        .excludePathPatterns(
            "/test/**",
            "/api/auth/**",
            "/api/auth/google/**",
            "/error", // 에러 응답 경로
            "/swagger-ui/**", // API 문서 (사용 시)
            "/v3/api-docs/**" // API 문서 (사용 시)
        );
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(loginUserArgumentResolver);
  }
}
