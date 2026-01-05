package com.dragons.infrastructure.post;

import com.dragons.domain.post.Post;
import com.dragons.domain.post.PostRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface PostJpaRepository extends PostRepository, JpaRepository<Post, Long> {

}
