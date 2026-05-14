package io.spring.articleservice.core.favorite;

import java.util.Optional;

// Repository interface for ArticleFavorite persistence — extracted from the monolith
public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);
}
