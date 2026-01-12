package com.dragons.domain.user;

import java.util.Optional;

public interface UserRepository {
  User save(User user);

  Optional<User> findById(Long userId);

  Optional<User> findByEmailAndProvider(String email, String provider);
}
