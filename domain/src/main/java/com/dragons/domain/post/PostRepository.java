package com.dragons.domain.post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepository {
  Post save(Post post);

  Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

  List<Post> findAll();

  Optional<Post> findById(Long postId);

  Optional<Post> findByIdAndDeletedAtIsNull(Long postId);
}
