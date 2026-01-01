package com.dragons.application.post.dto;

import java.util.List;

public record PostSearchResult(
        List<PostSummary> posts,
        long total,
        int page,
        int size) {
    public record PostSummary(
            long id,
            String title,
            String category,
            String author) {
    }
}
