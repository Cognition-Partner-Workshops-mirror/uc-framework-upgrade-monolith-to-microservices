package io.spring.articleservice.domain.favorite;

import java.util.Optional;

/**
 * ArticleFavorite repository — extracted from io.spring.core.favorite.ArticleFavoriteRepository.
 */
public interface ArticleFavoriteRepository {
  void save(ArticleFavorite articleFavorite);

  Optional<ArticleFavorite> find(String articleId, String userId);

  void remove(ArticleFavorite favorite);
}
