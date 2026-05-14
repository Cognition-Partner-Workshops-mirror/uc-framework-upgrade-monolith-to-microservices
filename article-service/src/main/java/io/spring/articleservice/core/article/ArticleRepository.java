package io.spring.articleservice.core.article;

import java.util.Optional;

// Repository interface for Article persistence — extracted from the monolith
public interface ArticleRepository {
  void save(Article article);

  Optional<Article> findById(String id);

  Optional<Article> findBySlug(String slug);

  void remove(Article article);
}
