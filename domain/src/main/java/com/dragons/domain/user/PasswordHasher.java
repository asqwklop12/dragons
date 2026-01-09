package com.dragons.domain.user;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordHasher {
  private final Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

  public String hashPassword(String plainPassword) {
    return encoder.encode(plainPassword);
  }

  public boolean verifyPassword(String plainPassword, String hashedPassword) {
    return encoder.matches(plainPassword, hashedPassword);
  }
}
