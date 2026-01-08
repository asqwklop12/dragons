package com.dragons.application.user.dto;

import java.time.ZonedDateTime;

public record UserLoginResult(
    String email,
    String name,
    ZonedDateTime LoginTime
) {
}
