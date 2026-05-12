package io.spring.articleservice.infrastructure.mybatis;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import io.spring.articleservice.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

// MyBatis-backed ArticleFavorite repository - extracted from monolith
@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
  private final ArticleFavoriteMapper articleFavoriteMapper;

  public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper articleFavoriteMapper) {
    this.articleFavoriteMapper = articleFavoriteMapper;
  }

  @Override
  public void save(ArticleFavorite articleFavorite) {
    articleFavoriteMapper.insert(articleFavorite);
  }

  @Override
  public Optional<ArticleFavorite> find(String articleId, String userId) {
    return Optional.ofNullable(articleFavoriteMapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleFavorite favorite) {
    articleFavoriteMapper.delete(favorite);
  }
}
