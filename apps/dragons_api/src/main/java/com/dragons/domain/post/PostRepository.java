package com.dragons.domain.post;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepository {
  Post save(Post post);

  Page<Post> findAll(Pageable pageable);

  Optional<Post> findById(Long postId);
}
