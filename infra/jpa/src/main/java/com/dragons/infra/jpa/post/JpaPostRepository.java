package com.dragons.infra.jpa.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByDeletedAtIsNull(Pageable pageable);

    java.util.Optional<PostEntity> findByIdAndDeletedAtIsNull(Long id);
}
