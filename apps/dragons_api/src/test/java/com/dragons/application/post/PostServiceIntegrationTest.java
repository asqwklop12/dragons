package com.dragons.application.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dragons.application.post.dto.PostDeleteCommand;
import com.dragons.application.post.dto.PostGetCommand;
import com.dragons.application.post.dto.PostGetResult;
import com.dragons.application.post.dto.PostSearchCondition;
import com.dragons.application.post.dto.PostSearchResult;
import com.dragons.application.post.dto.PostUpdateCommand;
import com.dragons.application.post.dto.PostUpdateResult;
import com.dragons.application.post.dto.PostWriteCommand;
import com.dragons.application.post.dto.PostWriteResult;
import com.dragons.config.jwt.JwtTokenProvider;
import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import com.dragons.infrastructure.post.PostJpaRepository;
import com.dragons.support.error.CoreException;
import com.dragons.utils.DatabaseCleanUp;
import com.dragons.utils.DragonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DragonIntegrationTest
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private String token;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        token = jwtTokenProvider.createAccessToken("yonghun");
    }

    @Test
    @DisplayName("게시글 작성")
    void write_success() {
        // given
        PostWriteCommand command = new PostWriteCommand(
                "제목",
                "내용",
                "backend",
                true,
                token);

        // when
        PostWriteResult result = postService.write(command);

        // then
        Post post = ((PostJpaRepository) postRepository).findAll().get(0);
        assertThat(post.title()).isEqualTo("제목");
        assertThat(post.content()).isEqualTo("내용");
        assertThat(post.category()).isEqualTo("backend");
        assertThat(post.author()).isEqualTo("yonghun");

        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.author()).isEqualTo("yonghun");
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void get_success() {
        // given
        Post saved = postRepository.save(Post.write("Title", "Content", "backend", true, "yonghun"));
        PostGetCommand command = new PostGetCommand(saved.getId());

        // when
        PostGetResult result = postService.get(command);

        // then
        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.content()).isEqualTo("Content");
        assertThat(result.author()).isEqualTo("yonghun");
        assertThat(result.category()).isEqualTo("backend");
    }

    @Test
    @DisplayName("게시글 수정")
    void update_success() {
        // given
        Post saved = postRepository.save(Post.write("Title", "Content", "backend", true, "yonghun"));
        PostUpdateCommand command = new PostUpdateCommand(saved.getId(), "Updated Title", "Updated Content");

        // when
        PostUpdateResult result = postService.update(command);

        // then
        Post updated = postRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.title()).isEqualTo("Updated Title");
        assertThat(updated.content()).isEqualTo("Updated Content");

        assertThat(result.title()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("게시글 삭제 - Soft Delete")
    void delete_success() {
        // given
        Post saved = postRepository.save(Post.write("Title", "Content", "backend", true, "yonghun"));
        PostDeleteCommand command = new PostDeleteCommand(saved.getId());

        // when
        postService.delete(command);

        // then
        Post deleted = postRepository.findById(saved.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 검색 - 페이징 및 정렬")
    void search_paging() {
        // given
        for (int i = 1; i <= 20; i++) {
            postRepository.save(Post.write("Title " + i, "Content " + i, "backend", true, "yonghun"));
        }

        PostSearchCondition condition = new PostSearchCondition(1, 10, "createdAt,desc");

        // when
        PostSearchResult result = postService.search(condition);

        // then
        assertThat(result.total()).isEqualTo(20);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.posts()).hasSize(10);

        // createdAt desc 이므로 가장 나중에 생성된 20번이 첫번째
        assertThat(result.posts().get(0).title()).isEqualTo("Title 20");
    }

    @Test
    @DisplayName("게시글 검색 - 정렬 조건 없음 (기본값)")
    void search_default_sort() {
        // given
        postRepository.save(Post.write("Old", "Content", "backend", true, "yonghun"));

        postRepository.save(Post.write("New", "Content", "backend", true, "yonghun"));

        PostSearchCondition condition = new PostSearchCondition(1, 10, null);

        // when
        PostSearchResult result = postService.search(condition);

        // then: 기본 정렬 createdAt desc
        assertThat(result.posts().get(0).title()).isEqualTo("New");
        assertThat(result.posts().get(1).title()).isEqualTo("Old");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외")
    void get_fail_notFound() {
        // given
        PostGetCommand command = new PostGetCommand(999L);

        // when & then
        assertThatThrownBy(() -> postService.get(command))
                .isInstanceOf(CoreException.class)
                .hasMessage("게시글이 존재하지 않습니다.");
    }
}
