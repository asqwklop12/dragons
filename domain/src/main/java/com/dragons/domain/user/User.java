package com.dragons.domain.user;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = true)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AuthProvider provider;

  @Column
  private ZonedDateTime loginTime;

  protected User() {
  }

  public static User register(String name, String email, String password) {
    validate(name, email, password);
    User user = new User();
    user.name = name;
    user.email = email;
    user.password = password;
    user.provider = AuthProvider.LOCAL;
    user.loginTime = null;
    return user;
  }

  public static User register(String email, String name) {
    validateEmailAndName(email, name);
    User user = new User();
    user.email = email;
    user.name = name;
    user.password = null; // OAuth 사용자는 비밀번호 불필요
    user.provider = AuthProvider.GOOGLE;
    user.loginTime = null;
    return user;
  }

  private static void validateEmailAndName(String email, String name) {
    validateEmail(email);
    validateName(name);
  }

  private static void validateEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
    }
    if (!EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException("유효한 이메일 형식이 아닙니다.");
    }
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
    }
    if (name.length() > 100) {
      throw new IllegalArgumentException("이름은 100자를 초과할 수 없습니다.");
    }
  }

  public static User withId(Long id, String name, String email, String password) {
    User user = new User();
    user.setIdForTest(id);
    user.name = name;
    user.email = email;
    user.password = password;
    return user;
  }

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private static void validate(String name, String email, String password) {
    validateEmail(email);
    validateName(name);

    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
    }
    if (password.length() < 8) {
      throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
    }
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

  private void setIdForTest(Long id) {
    try {
      java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(this, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public void loginUpdateTime(final Clock clock) {
    this.loginTime = ZonedDateTime.now(clock);
  }

  @Getter
  @AllArgsConstructor
  public enum AuthProvider {
    LOCAL("LOCAL"),    // 일반 이메일/비밀번호 가입
    GOOGLE("GOOGLE");   // 구글 OAuth
    // 향후 KAKAO, NAVER 등 추가 가능
    private final String value;


  }
}
