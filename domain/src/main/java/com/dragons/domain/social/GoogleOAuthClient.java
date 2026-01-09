package com.dragons.domain.social;

public interface GoogleOAuthClient {
  GoogleOAuthResponse getUserInfo(String code);
}
