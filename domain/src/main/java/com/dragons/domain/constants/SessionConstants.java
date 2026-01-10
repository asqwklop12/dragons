package com.dragons.domain.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionConstants {
  SESSION_USER_EMAIL("userEmail");
  private final String value;

}
