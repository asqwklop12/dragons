package com.dragons.application.post.dto;

public record PostWriteResult(
    String title,
    String content,
    String category,
    boolean isPublic,
    String author // 만든이
) {
}
