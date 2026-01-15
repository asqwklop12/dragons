package com.dragons.infra.toss;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "pg.toss")
@Getter
@Setter
public class PgProperties {

    private String clientKey;
    private String secretKey;
    private String baseUrl = "https://api.tosspayments.com/v1";

}
