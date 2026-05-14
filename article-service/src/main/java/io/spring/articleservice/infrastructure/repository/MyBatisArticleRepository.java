package io.spring.articleservice.infrastructure.repository;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

// MyBatis-backed repository implementation for Article persistence
@Repository
public class MyBatisArticleRepository implements ArticleRepository {

  private final ArticleMapper articleMapper;

  public MyBatisArticleRepository(ArticleMapper articleMapper) {
    this.articleMapper = articleMapper;
  }

  @Override
  public void save(Article article) {
    // Check if article already exists to determine insert vs update
    if (articleMapper.findById(article.getId()) == null) {
      createNewArticle(article);
    } else {
      articleMapper.update(article);
    }
  }

  // Insert article and its associated tags
  private void createNewArticle(Article article) {
    for (Tag tag : article.getTags()) {
      // Insert tag if not already present (idempotent)
      if (articleMapper.findTag(tag.getName()) == null) {
        articleMapper.insertTag(tag);
      }
      articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
    }
    articleMapper.insert(article);
  }

  @Override
  public Optional<Article> findById(String id) {
    return Optional.ofNullable(articleMapper.findById(id));
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  @Override
  public void remove(Article article) {
    articleMapper.delete(article.getId());
  }
}
