package com.dragons.application.user;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
  private final Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

  public String hashPassword(String plainPassword) {
    if (plainPassword == null || plainPassword.isBlank()) {
      throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
    }
    return encoder.encode(plainPassword);
  }

  public boolean verifyPassword(String plainPassword, String hashedPassword) {
    if (plainPassword == null || hashedPassword == null) {
      return false;
    }
    return encoder.matches(plainPassword, hashedPassword);
  }
}
