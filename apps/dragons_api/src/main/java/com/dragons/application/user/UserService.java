package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginCommand;
import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.application.user.dto.UserRegisterCommand;
import com.dragons.application.user.dto.UserRegisterResult;
import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import jakarta.transaction.Transactional;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final Clock clock;

  @Transactional
  public UserRegisterResult register(UserRegisterCommand command) {
    User saved = userRepository.save(User.register(command.name(), command.email(), command.password()));
    return new UserRegisterResult(saved.name(), saved.email());
  }

  @Transactional
  public UserLoginResult login(UserLoginCommand command) {
    User user = userRepository.findByEmailAndProvider(command.email(),"LOCAL")
        .orElseThrow(() -> new CoreException(ErrorType.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다"));

    try {
      user.matchPassword(command.password());
    } catch (IllegalArgumentException e) {
      throw new CoreException(ErrorType.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다");
    }
    user.loginUpdateTime(clock);

    return new UserLoginResult(
        user.email(),
        user.name(),
        user.getLoginTime()
    );
  }
}
