package io.spring.article.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Domain entity representing a user's favorite on an article
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ArticleFavorite {
  private String articleId;
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
