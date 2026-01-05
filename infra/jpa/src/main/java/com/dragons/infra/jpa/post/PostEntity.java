package com.dragons.infra.jpa.post;

import com.dragons.infra.jpa.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "boards")
@Getter
public class PostEntity extends BaseEntity {
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    public PostEntity(String title, String content, String category, String author, boolean isPublic) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.author = author;
        this.isPublic = isPublic;
    }

    public void update(String title, String content) {
        if (title != null)
            this.title = title;
        if (content != null)
            this.content = content;
    }
}
