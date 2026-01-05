package com.dragons.domain.post;

import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class Post {
  private Long id;
  private ZonedDateTime createdAt;
  private ZonedDateTime updatedAt;
  private ZonedDateTime deletedAt;

  private String title;
  private String content;
  private String category;
  private String author;
  private boolean isPublic;

  public static Post write(String title, String content, String category, boolean isPublic, String author) {
    return new Post(null, title, content, category, author, isPublic, null, null, null);
  }

  // Constructor for New Creation
  private Post(Long id, String title, String content, String category, String author, boolean isPublic,
      ZonedDateTime createdAt, ZonedDateTime updatedAt, ZonedDateTime deletedAt) {
    validate(title, content, category, author);
    this.id = id;
    this.title = title;
    this.content = content;
    this.category = category;
    this.author = author;
    this.isPublic = isPublic;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  // Constructor for Reconstruction from Persistence
  public static Post withId(Long id, String title, String content, String category, String author, boolean isPublic,
      ZonedDateTime createdAt, ZonedDateTime updatedAt, ZonedDateTime deletedAt) {
    // Bypass validation or keep it? explicit constructor typically bypasses purely
    // creation logic but keeps invariant checks.
    // reusing the private constructor
    return new Post(id, title, content, category, author, isPublic, createdAt, updatedAt, deletedAt);
  }

  private void validate(String title, String content, String category, String author) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("제목은 필수입니다");
    }
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("내용은 필수입니다");
    }
    if (category == null || category.isBlank()) {
      throw new IllegalArgumentException("카테고리는 필수입니다");
    }
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("작성자는 필수입니다");
    }
  }

  public String title() {
    return title;
  }

  public String content() {
    return content;
  }

  public String category() {
    return category;
  }

  public String author() {
    return author;
  }

  public void reWrite(String title, String content) {
    if (title != null) {
      this.title = title;
    }

    if (content != null) {
      this.content = content;
    }
    this.updatedAt = ZonedDateTime.now(); // Manually update timestamp (or let Infra handle it on save)
  }

  public void checkAuthor(String author) {
    if (!this.author.equals(author)) {
      throw new IllegalArgumentException("작성자가 일치하지 않습니다.");
    }
  }

  public void delete() {
    if (this.deletedAt == null) {
      this.deletedAt = ZonedDateTime.now();
    }
  }

  public void restore() {
    if (this.deletedAt != null) {
      this.deletedAt = null;
    }
  }
}
