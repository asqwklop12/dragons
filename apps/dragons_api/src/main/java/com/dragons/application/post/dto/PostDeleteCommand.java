package com.dragons.application.post.dto;

public record PostDeleteCommand(
    Long postId,
    String token
) {
}
