package com.dragons.domain.user;

import com.dragons.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "email", nullable = false, length = 255, unique = true)
  private String email;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  public String name() {
    return name;
  }

  public String email() {
    return email;
  }

  public String password() {
    return password;
  }
}
