package com.dragons.application.post.dto;

public record PostUpdateCommand(
    Long postId,
    String title,
    String content
) {
}
