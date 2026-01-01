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

  public static Post write(String title, String content, String category, boolean isPublic, String author) {
    return new Post(title, content, category, isPublic, author);
  }

  private Post(String title, String content, String category, boolean isPublic, String author) {
    this.title = title;
    this.content = content;
    this.category = category;
    this.author = author;
    this.isPublic = isPublic;
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
    if(title != null) {
      this.title = title;
    }

    if(content != null) {
      this.content = content;
    }
  }

  public void checkAuthor(String author) {
    if(!this.author.equals(author)) {
      throw new IllegalArgumentException("작성자가 일치하지 않습니다.");
    }
  }
}
