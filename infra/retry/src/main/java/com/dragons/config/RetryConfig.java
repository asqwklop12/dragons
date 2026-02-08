package com.dragons.config;

import com.dragons.executor.RetryExecutor;
import com.dragons.policy.DefaultRetryPolicy;
import com.dragons.policy.RetryPolicy;
import com.dragons.policy.SocialLoginRetryPolicy;
import com.dragons.properties.RetryProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {

  @Primary
  @Bean
  public RetryPolicy retryPolicy(RetryProperties properties) {
    return new DefaultRetryPolicy(properties.policies().get("default"));
  }

  @Primary
  @Bean
  public RetryExecutor retryExecutor(RetryPolicy retryPolicy) {
    return new RetryExecutor(retryPolicy);
  }

  @Bean
  public RetryPolicy retrySocialPolicy(RetryProperties properties) {
    return new SocialLoginRetryPolicy(properties.policies().get("social-login"));
  }

  @Bean
  public RetryExecutor socialRetryExecutor(@Qualifier("retrySocialPolicy") RetryPolicy retrySocialPolicy) {
    return new RetryExecutor(retrySocialPolicy);
  }

}
