package com.dragons.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dragons.domain.user.User;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    @DisplayName("게시글 생성 테스트")
    void create() {
        // given
        String title = "제목";
        String content = "내용";
        String category = "backend";
        String author = "author";
        boolean isPublic = true;

        // when
        Post post = Post.write(title, content, category, isPublic, author);

        // then
        assertThat(post.title()).isEqualTo(title);
        assertThat(post.content()).isEqualTo(content);
        assertThat(post.category()).isEqualTo(category);
        assertThat(post.author()).isEqualTo(author);
        assertThat(post.isPublic()).isTrue();
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 제목과 내용 수정")
    void rewrite() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author");

        // when
        post.reWrite("수정된 제목", "수정된 내용");

        // then
        assertThat(post.title()).isEqualTo("수정된 제목");
        assertThat(post.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 제목만 수정")
    void rewrite_title_only() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author");

        // when
        post.reWrite("수정된 제목", null);

        // then
        assertThat(post.title()).isEqualTo("수정된 제목");
        assertThat(post.content()).isEqualTo("내용");
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 내용만 수정")
    void rewrite_content_only() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author");

        // when
        post.reWrite(null, "수정된 내용");

        // then
        assertThat(post.title()).isEqualTo("제목");
        assertThat(post.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("게시글 삭제(Soft Delete) 테스트")
    void delete() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author");

        // when
        post.delete();

        // then
        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 작성자와 로그인한 사용자가 다를 시 예외 발생")
    void checkAuthor() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author2");
        String author = "author1";

        // when & then
        assertThatThrownBy(() -> post.checkAuthor(author));
    }

    @Test
    @DisplayName("게시글 복구 테스트")
    void restore() {
        // given
        Post post = Post.write("제목", "내용", "backend", true, "author");
        post.delete();
        assertThat(post.getDeletedAt()).isNotNull();

        // when
        post.restore();

        // then
        assertThat(post.getDeletedAt()).isNull();
    }


    @Nested
    @DisplayName("실패: 공백 검증")
    class Validate {

        @Test
        @DisplayName("제목이 비어있으면 IllegalArgumentException")
        void notNull_title() {
            assertThatThrownBy(() -> Post.write(" \t\n", "내용", "backend", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("제목은 필수입니다");
        }

        @Test
        @DisplayName("내용이 비어있으면 IllegalArgumentException")
        void notNull_content() {
            assertThatThrownBy(() -> Post.write("제목1", "\t\n", "backend", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("내용은 필수입니다");
        }

        @Test
        @DisplayName("카테고리가 비어있으면 IllegalArgumentException")
        void notNull_category() {
            assertThatThrownBy(() -> Post.write("제목1", "내용", "\t\n", true, "author"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카테고리는 필수입니다");
        }

        @Test
        @DisplayName("작성자가 비어있으면 IllegalArgumentException")
        void notNull_author() {
            assertThatThrownBy(() -> Post.write("제목1", "내용", "backend", true, "\t\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("작성자는 필수입니다");
        }






    }
}
