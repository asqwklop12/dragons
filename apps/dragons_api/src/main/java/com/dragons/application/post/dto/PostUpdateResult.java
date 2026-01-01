package com.dragons.application.post.dto;

public record PostUpdateResult(
    long id,
    String title,
    String author
) {
}
