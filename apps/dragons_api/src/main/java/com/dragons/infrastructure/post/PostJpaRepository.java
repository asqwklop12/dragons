package com.dragons.infrastructure.post;

import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends PostRepository, JpaRepository<Post, Long> {
  Optional<Post> findByIdAndDeletedAtIsNull(Long postId);
}
