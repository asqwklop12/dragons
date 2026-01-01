package com.dragons.application.post.dto;

public record PostGetResult(
    long id,
    String title,
    String content,
    String category,
    String author
) {
}
