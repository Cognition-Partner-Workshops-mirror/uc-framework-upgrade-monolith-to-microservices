package io.spring.article.infrastructure.repository;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.article.Tag;
import io.spring.article.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MyBatisArticleRepository implements ArticleRepository {

  private ArticleMapper articleMapper;

  @Override
  public void save(Article article) {
    if (articleMapper.findById(article.getId()) == null) {
      articleMapper.insert(article);
      for (Tag tag : article.getTags()) {
        articleMapper.insertTag(tag);
        articleMapper.insertArticleTagRelation(article.getId(), tag.getId());
      }
    } else {
      articleMapper.update(article);
    }
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
