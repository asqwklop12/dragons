package com.dragons.infra.jpa.post;

import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final JpaPostRepository jpaPostRepository;

    @Override
    public Post save(Post post) {
        PostEntity entity;
        if (post.getId() != null) {
            entity = jpaPostRepository.findById(post.getId())
                    .orElse(new PostEntity(post.title(), post.content(), post.category(), post.author(),
                            post.isPublic()));
            entity.update(post.title(), post.content());
        } else {
            entity = new PostEntity(post.title(), post.content(), post.category(), post.author(), post.isPublic());
        }

        // Sync deletedAt
        if (post.getDeletedAt() != null) {
            if (entity.getDeletedAt() == null) {
                entity.delete();
            }
        } else {
            if (entity.getDeletedAt() != null) {
                entity.restore();
            }
        }

        return toDomain(jpaPostRepository.save(entity));
    }

    @Override
    public Page<Post> findAllByDeletedAtIsNull(Pageable pageable) {
        return jpaPostRepository.findAllByDeletedAtIsNull(pageable).map(this::toDomain);
    }

    @Override
    public List<Post> findAll() {
        return jpaPostRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return jpaPostRepository.findById(postId).map(this::toDomain);
    }

    @Override
    public Optional<Post> findByIdAndDeletedAtIsNull(Long postId) {
        return jpaPostRepository.findByIdAndDeletedAtIsNull(postId).map(this::toDomain);
    }

    private Post toDomain(PostEntity entity) {
        return Post.withId(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getCategory(),
                entity.getAuthor(),
                entity.isPublic(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
