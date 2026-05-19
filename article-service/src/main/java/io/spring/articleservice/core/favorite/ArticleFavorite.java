package io.spring.articleservice.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// ArticleFavorite entity extracted from monolith. Favorites are part of the Article bounded context.
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
