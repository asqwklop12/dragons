package com.dragons.application.post.dto;

public record PostSearchCondition(
    int page,
    int limit,
    String sort
) {
}
