package com.dragons.application.post.dto;

public record PostWriteCommand(
    String title,
    String content,
    String category,
    boolean isPublic,
    String author // author
) {
}
