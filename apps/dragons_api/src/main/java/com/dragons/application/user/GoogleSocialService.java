package com.dragons.application.user;

import com.dragons.application.user.dto.UserLoginResult;
import com.dragons.domain.social.GoogleOAuthClient;
import com.dragons.domain.social.GoogleOAuthResponse;
import com.dragons.domain.user.User;
import com.dragons.domain.user.User.AuthProvider;
import com.dragons.domain.user.UserRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSocialService {
  private final UserRepository userRepository;
  private final GoogleOAuthClient googleOAuthClient;
  private final Clock clock;

  @Transactional
  public UserLoginResult loginWithGoogle(String code) {
    if (code == null || code.isBlank()) {
      throw new CoreException(ErrorType.BAD_REQUEST, "인증 코드가 필요합니다");
    }
    try {
      GoogleOAuthResponse response = googleOAuthClient.getUserInfo(code);
      User user = userRepository.findByEmailAndProvider(response.email(), AuthProvider.GOOGLE.getValue())
          .orElseGet(() -> userRepository.save(User.register(response.email(), response.name()))); // 없으면 가입

      user.loginUpdateTime(clock);
      return new UserLoginResult(user.email(), user.name(), user.getLoginTime());
    } catch (CoreException e) {
      throw e; // 이미 처리된 예외는 재throw
    } catch (Exception e) {
      // 로그에는 상세 정보 기록, 사용자에게는 일반 메시지만 표시
      log.error("구글 OAuth 처리 중 예외 발생", e);
      throw new CoreException(ErrorType.INTERNAL_ERROR, "구글 로그인 처리 중 오류가 발생했습니다");
    }
  }


  public String getGoogleLoginUrl() {
    return googleOAuthClient.getGoogleLoginUrl();
  }
}
