package com.dragons.application.post.dto;

/**
 * 게시글 조회 결과
 *
 * @param id       게시글 ID
 * @param title    제목 (non-null)
 * @param content  내용 (non-null)
 * @param category 카테고리 (nullable)
 * @param author   작성자 (non-null)
 */
public record PostGetResult(
    long id,
    String title,
    String content,
    String category,
    String author
) {
}
