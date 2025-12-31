package com.dragons.domain.user;

import com.dragons.domain.BaseEntity;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.regex.Pattern;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;


  public static User register(String name, String email, String password) {
    validate(name, email, password);
    return new User(name, email, password);
  }

  private static void validate(String name, String email, String password) {
    if (name == null || name.isBlank()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "이름은 비어 있을 수 없습니다.");
    }
    if (name.length() > 100) {
      throw new CoreException(ErrorType.BAD_REQUEST, "이름은 100자를 초과할 수 없습니다.");
    }

    if (email == null || email.isBlank()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 비어 있을 수 없습니다.");
    }
    // 간단한 이메일 형식 검증 (RFC 완전 준수 아님)
    Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    if (!emailPattern.matcher(email).matches()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "유효한 이메일 형식이 아닙니다.");
    }

    if (password == null || password.isBlank()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 비어 있을 수 없습니다.");
    }
    if (password.length() < 8) {
      throw new CoreException(ErrorType.BAD_REQUEST, "비밀번호는 최소 8자 이상이어야 합니다.");
    }
  }

  private User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
  }

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
