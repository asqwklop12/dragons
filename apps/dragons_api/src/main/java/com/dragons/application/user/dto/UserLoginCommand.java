package com.dragons.application.user.dto;

public record UserLoginCommand(
    String email,
    String password
) {
}
