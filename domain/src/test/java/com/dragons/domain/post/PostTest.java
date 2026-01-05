package com.dragons.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    @DisplayName("게시글 작성 성공")
    void write_success() {
        Post post = Post.write("제목", "내용", "backend", true, "author");

        assertThat(post.title()).isEqualTo("제목");
        assertThat(post.content()).isEqualTo("내용");
        assertThat(post.category()).isEqualTo("backend");
        assertThat(post.author()).isEqualTo("author");
        assertThat(post.isPublic()).isTrue();
        assertThat(post.getId()).isNull();
        assertThat(post.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("게시글 작성 실패 - 유효성 검증")
    void write_fail_validation() {
        assertThatThrownBy(() -> Post.write("", "내용", "backend", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("제목은 필수입니다");

        assertThatThrownBy(() -> Post.write("제목", "", "backend", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("내용은 필수입니다");

        assertThatThrownBy(() -> Post.write("제목", "내용", "", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리는 필수입니다");

        assertThatThrownBy(() -> Post.write("제목", "내용", "backend", true, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자는 필수입니다");
    }

    @Test
    @DisplayName("게시글 수정 - reWrite")
    void reWrite_success() {
        Post post = Post.write("제목", "내용", "backend", true, "author");

        post.reWrite("새 제목", "새 내용");

        assertThat(post.title()).isEqualTo("새 제목");
        assertThat(post.content()).isEqualTo("새 내용");
        assertThat(post.content()).isEqualTo("새 내용");
    }

    @Test
    @DisplayName("게시글 작성자 확인")
    void checkAuthor() {
        Post post = Post.write("제목", "내용", "backend", true, "author");

        // Success case
        post.checkAuthor("author");

        // Fail case
        assertThatThrownBy(() -> post.checkAuthor("other"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("게시글 삭제 및 복구")
    void delete_restore() {
        Post post = Post.write("제목", "내용", "backend", true, "author");

        post.delete();
        assertThat(post.getDeletedAt()).isNotNull();

        post.restore();
        assertThat(post.getDeletedAt()).isNull();
    }
}
