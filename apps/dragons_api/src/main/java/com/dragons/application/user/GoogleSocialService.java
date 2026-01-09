package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.domain.social.GoogleOAuthClient;
import com.dragons.domain.social.GoogleOAuthResponse;
import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleSocialService {
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final Clock clock;

  @Transactional
  public UserLoginResult loginWithGoogle(String code) {
    GoogleOAuthResponse response = googleOAuthClient.getUserInfo(code);

    User user = userRepository.findByEmail(response.email())
        .orElseGet(() -> userRepository.save(User.register(response.email(), response.name()))); // 없으면 가입

    user.loginUpdateTime(clock);

    return new UserLoginResult(user.getEmail(), user.getName(), user.getLoginTime());
  }

}
