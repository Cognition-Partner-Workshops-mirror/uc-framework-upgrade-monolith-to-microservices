package io.spring.articleservice.core.article;

import java.util.Optional;

// Repository interface for Article persistence - mirrors monolith ArticleRepository
public interface ArticleRepository {
  void save(Article article);

  Optional<Article> findById(String id);

  Optional<Article> findBySlug(String slug);

  void remove(Article article);
}
