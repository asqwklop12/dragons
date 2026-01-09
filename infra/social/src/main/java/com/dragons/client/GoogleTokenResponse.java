package com.dragons.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") Integer expiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("scope") String scope,
    @JsonProperty("refresh_token") String refreshToken

) {
}
