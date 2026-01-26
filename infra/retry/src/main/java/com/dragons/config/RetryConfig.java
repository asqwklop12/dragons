package com.dragons.config;

import com.dragons.executor.RetryExecutor;
import com.dragons.policy.DefaultRetryPolicy;
import com.dragons.policy.RetryPolicy;
import com.dragons.properties.RetryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class RetryConfig {


  @Bean
  public RetryPolicy retryPolicy(RetryProperties properties) {
    return new DefaultRetryPolicy(properties.policies().get("default"));
  }

  @Bean
  public RetryExecutor retryExecutor(RetryPolicy retryPolicy) {
    return new RetryExecutor(retryPolicy);
  }

}
