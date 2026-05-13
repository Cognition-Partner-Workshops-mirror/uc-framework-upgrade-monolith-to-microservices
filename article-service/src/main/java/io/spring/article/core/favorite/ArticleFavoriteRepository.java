package io.spring.article.core.favorite;

import java.util.Optional;

// Repository interface for ArticleFavorite persistence.
public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);

  int countByArticleId(String articleId);

  boolean isFavorited(String articleId, String userId);
}
