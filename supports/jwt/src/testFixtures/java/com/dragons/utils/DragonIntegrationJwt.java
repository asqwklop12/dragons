package com.dragons.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.TestPropertySource;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestPropertySource(properties = {
        "jwt.header=Authorization",
        "jwt.issuer=dragons-test",
        "jwt.client-secret=test-secret-key-that-is-long-enough-for-hs512-algorithm-minimum-64-bytes",
        "jwt.access-token-validity-in-seconds=3600",
        "jwt.refresh-token-validity-in-seconds=86400"
})
public @interface DragonIntegrationJwt {
}
