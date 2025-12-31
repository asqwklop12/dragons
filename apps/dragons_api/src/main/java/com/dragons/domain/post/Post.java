package com.dragons.domain.post;

import com.dragons.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "boards")
public class Post extends BaseEntity {
  @Column(name = "title", nullable = false, length = 200)
  private String title;

  // 본문은 길이가 길 수 있으므로 TEXT로 지정
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "category", nullable = false, length = 100)
  private String category;

  @Column(name = "author", nullable = false, length = 100)
  private String author;

  @Getter
  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

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
}
