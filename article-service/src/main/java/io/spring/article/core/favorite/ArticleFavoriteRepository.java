package io.spring.article.core.favorite;

import java.util.Optional;

// Repository interface for article favorite persistence operations
public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);
}
