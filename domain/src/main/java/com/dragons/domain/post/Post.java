package com.dragons.domain.post;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "boards")
public class Post extends BaseEntity {

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 10000)
  private String content;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String author;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

  protected Post() {
  }

  public static Post write(String title, String content, String category, boolean isPublic, String author) {
    validate(title, content, category, author);
    Post post = new Post();
    post.title = title;
    post.content = content;
    post.category = category;
    post.isPublic = isPublic;
    post.author = author;
    return post;
  }

  // Constructor for Reconstruction from tests or other layers if needed, though
  // usually JPA handles retrieval
  // Keeping this for compatibility with existing tests
  public static Post withId(Long id, String title, String content, String category, String author, boolean isPublic) {
    Post post = new Post();
    post.setIdForTest(id);
    post.title = title;
    post.content = content;
    post.category = category;
    post.author = author;
    post.isPublic = isPublic;
    return post;
  }

  private static void validate(String title, String content, String category, String author) {
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
  }

  public void checkAuthor(String author) {
    if (!this.author.equals(author)) {
      throw new IllegalArgumentException("작성자가 일치하지 않습니다.");
    }
  }

  private void setIdForTest(Long id) {
    // Reflection or protected setter in BaseEntity needed if we want to set ID
    // manually for tests without reflection
    // For now, assuming BaseEntity might not expose setId.
    // Actually BaseEntity usually generates ID.
    // If we need to set ID, we might need a hack or change BaseEntity.
    // Let's modify BaseEntity to allow protected setId or assume test uses
    // reflection if it's strictly unit test.
    // But wait, "withId" factory implies creating an object with ID.
    // Since we removed 'id' field from here and it's in BaseEntity (private), we
    // can't set it easily.
    // We will touch BaseEntity to add a protected setter for id, or just use
    // reflection here.
    try {
      java.lang.reflect.Field idField = BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(this, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
