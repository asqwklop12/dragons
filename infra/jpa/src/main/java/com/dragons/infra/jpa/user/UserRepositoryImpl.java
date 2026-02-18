package com.dragons.infra.jpa.user;

import com.dragons.domain.user.User;
import com.dragons.domain.user.User.AuthProvider;
import com.dragons.domain.user.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository jpaUserRepository;

  @Override
  public User save(User user) {
    return jpaUserRepository.save(user);
  }

  @Override
  public Optional<User> findById(Long userId) {
    return jpaUserRepository.findById(userId);
  }

  @Override
  public Optional<User> findByEmailAndProvider(String email, String provider) {

    AuthProvider authProvider;
    try {
      authProvider = AuthProvider.valueOf(provider);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid provider string: {}", provider);
      return Optional.empty();
    }
    return jpaUserRepository.findByEmailAndProvider(email, authProvider);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email);
  }
}
