package com.dragons.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("회원가입 성공")
    void register_success() {
        User user = User.register("홍길동", "test@example.com", "password123!");

        assertThat(user.name()).isEqualTo("홍길동");
        assertThat(user.email()).isEqualTo("test@example.com");
        assertThat(user.password()).isEqualTo("password123!");
        assertThat(user.getId()).isNull();
        assertThat(user.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("회원가입 실패 - 유효성 검증")
    void register_fail_validation() {
        // 이름
        assertThatThrownBy(() -> User.register("", "test@example.com", "password123!"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> User.register("a".repeat(101), "test@example.com", "password123!"))
                .isInstanceOf(IllegalArgumentException.class);

        // 이메일
        assertThatThrownBy(() -> User.register("홍길동", "", "password123!"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> User.register("홍길동", "invalid-email", "password123!"))
                .isInstanceOf(IllegalArgumentException.class);

        // 비밀번호
        assertThatThrownBy(() -> User.register("홍길동", "test@example.com", ""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> User.register("홍길동", "test@example.com", "short"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원 탈퇴 및 복구")
    void delete_restore() {
        User user = User.register("홍길동", "test@example.com", "password123!");

        user.delete();
        assertThat(user.getDeletedAt()).isNotNull();

        user.restore();
        assertThat(user.getDeletedAt()).isNull();
    }
}
