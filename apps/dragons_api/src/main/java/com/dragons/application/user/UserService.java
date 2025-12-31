package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.application.user.dto.UserRegisterResult;
import com.dragons.config.jwt.JwtTokenProvider;
import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import jakarta.transaction.Transactional;
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

  public UserLoginResult login(UserLoginCommand command) {
    return new UserLoginResult(jwtTokenProvider.createAccessToken(command.email()));
  }
}
