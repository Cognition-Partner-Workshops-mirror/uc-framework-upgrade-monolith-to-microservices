package io.spring.article.core.article;

import java.util.Optional;

// Repository interface for article persistence operations
public interface ArticleRepository {

  void save(Article article);

  Optional<Article> findById(String id);

  Optional<Article> findBySlug(String slug);

  void remove(Article article);
}
