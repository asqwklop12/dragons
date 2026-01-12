package com.dragons.infra.jpa.post;

import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class PostRepositoryImpl implements PostRepository {
    private final JpaPostRepository jpaPostRepository;

    @Override
    public Post save(Post post) {
        return jpaPostRepository.save(post);
    }

    @Override
    public Page<Post> findAllByDeletedAtIsNull(Pageable pageable) {
        return jpaPostRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public List<Post> findAll() {
        return jpaPostRepository.findAll();
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return jpaPostRepository.findById(postId);
    }

    @Override
    public Optional<Post> findByIdAndDeletedAtIsNull(Long postId) {
        return jpaPostRepository.findByIdAndDeletedAtIsNull(postId);
    }
}
