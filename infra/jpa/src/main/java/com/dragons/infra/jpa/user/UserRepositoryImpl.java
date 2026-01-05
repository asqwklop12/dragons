package com.dragons.infra.jpa.user;

import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        // If logic is register, usually ID is null.
        UserEntity entity = new UserEntity(user.getName(), user.getEmail(), user.getPassword());

        // Handle ID if present for updates (omitted for now as register is main use
        // case shown)

        return toDomain(jpaUserRepository.save(entity));
    }

    private User toDomain(UserEntity entity) {
        return User.withId(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
