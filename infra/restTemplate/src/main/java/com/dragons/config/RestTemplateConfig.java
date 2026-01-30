package com.dragons.config;

import com.dragons.interceptor.MdcInterceptor;
import com.dragons.interceptor.RequestResponseLoggingInterceptor;
import com.dragons.properties.RestTemplateProperties;
import com.dragons.properties.RestTemplateProperties.RestTemplateProperty;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(RestTemplateProperties.class)
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder, Environment env, RestTemplateProperties properties) {
    boolean isProd = env.acceptsProfiles(Profiles.of("prod"));
    RestTemplateProperty property = properties.policies().get("default");
    return builder
        .connectTimeout(Duration.ofSeconds(property.connectTimeout()))
        .readTimeout(Duration.ofSeconds(property.readTimeout()))
        .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
        .additionalInterceptors(new MdcInterceptor(), new RequestResponseLoggingInterceptor(isProd))
        .build();
  }
}
