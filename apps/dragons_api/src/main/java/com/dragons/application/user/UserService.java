package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.application.user.dto.UserRegisterResult;
import com.dragons.config.jwt.JwtTokenProvider;
import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public UserRegisterResult register(UserRegisterCommand command) {
    User saved = userRepository.save(User.register(command.name(), command.email(), command.password()));
    return new UserRegisterResult(saved.name(), saved.email());
  }

  @Transactional
  public UserLoginResult login(UserLoginCommand command) {
    Optional<User> byEmail = userRepository.findByEmail(command.email());
    if (byEmail.isEmpty()) {
      throw new CoreException(ErrorType.NOT_FOUND, "이메일이 존재하지 않습니다");
    }

    User user = byEmail.get();
    user.matchPassword(command.password());
    user.loginUpdateTime();

    return new UserLoginResult(
        user.email(),
        user.name(),
        user.getLoginTime()
    );
  }
}
