package com.dragons.domain.social;

public interface GoogleOAuthClient {
  GoogleOAuthResponse getUserEmail(String code);
}
