package com.dragons.config;

import com.dragons.interceptor.MdcInterceptor;
import com.dragons.interceptor.RequestResponseLoggingInterceptor;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder, Environment env) {
    boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
    return builder
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(10))
        .additionalInterceptors(new MdcInterceptor())
        .additionalInterceptors(new RequestResponseLoggingInterceptor(isProd))
        .build();
  }
}
