package com.dragons.infrastructure.user;

import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends UserRepository, JpaRepository<User,Long> {
}
