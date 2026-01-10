package com.dragons.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleUserInfoResponse(
    String email,
    String name,
    String picture,
    @JsonProperty("verified_email") Boolean verifiedEmail
) {
}
