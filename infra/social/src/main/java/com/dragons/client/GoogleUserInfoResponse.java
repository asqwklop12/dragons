package com.dragons.client;

public record GoogleUserInfoResponse(
    String email,
    String name,
    String picture,
    Boolean verified_email
) {
}
