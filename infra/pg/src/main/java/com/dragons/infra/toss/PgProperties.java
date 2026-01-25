package com.dragons.infra.toss;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pg.toss")
@Getter
@Setter
public class PgProperties {

    @NotBlank (message = "pg.toss.client-key는 필수입니다")
    private String clientKey;
    @NotBlank(message = "pg.toss.secret-key는 필수입니다")
    private String secretKey;
    private String baseUrl = "https://api.tosspayments.com/v1";

}
