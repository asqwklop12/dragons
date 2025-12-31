package com.dragons.application.user.dto;

public record UserRegisterCommand(
    String name,
    String email,
    String password
) { }
