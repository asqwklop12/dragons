package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.domain.social.GoogleOAuthClient;
import com.dragons.domain.social.GoogleOAuthResponse;
import com.dragons.domain.user.User;
import com.dragons.domain.user.UserRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleSocialService {
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final Clock clock;

  public UserLoginResult loginWithGoogle(String code) {
    try {
      GoogleOAuthResponse response = googleOAuthClient.getUserInfo(code);
      return createOrUpdateUser(response);
    } catch (Exception e) {
      throw new CoreException(ErrorType.INTERNAL_ERROR, "구글 로그인 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  @Transactional
  public UserLoginResult createOrUpdateUser(GoogleOAuthResponse response) {
    User user = userRepository.findByEmailAndProvider(response.email(),"LOCAL")
        .orElseGet(() -> userRepository.save(User.register(response.email(), response.name()))); // 없으면 가입

    user.loginUpdateTime(clock);
    return new UserLoginResult(user.email(), user.name(), user.getLoginTime());
  }

  public String getGoogleLoginUrl() {
    return googleOAuthClient.getGoogleLoginUrl();
  }
}
