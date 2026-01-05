package com.dragons.infra.jpa.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.dragons.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

    java.util.Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
