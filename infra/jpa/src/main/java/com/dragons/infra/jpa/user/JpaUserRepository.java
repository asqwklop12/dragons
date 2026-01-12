package com.dragons.infra.jpa.user;

import com.dragons.domain.user.User;
import com.dragons.domain.user.User.AuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndProvider(String email, AuthProvider provider);
}
