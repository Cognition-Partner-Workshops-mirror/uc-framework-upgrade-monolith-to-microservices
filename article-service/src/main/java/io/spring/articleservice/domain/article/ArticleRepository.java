package io.spring.articleservice.domain.article;

import java.util.Optional;

/** Article repository interface — extracted from io.spring.core.article.ArticleRepository. */
public interface ArticleRepository {
  void save(Article article);

  Optional<Article> findById(String id);

  Optional<Article> findBySlug(String slug);

  void remove(Article article);
}
