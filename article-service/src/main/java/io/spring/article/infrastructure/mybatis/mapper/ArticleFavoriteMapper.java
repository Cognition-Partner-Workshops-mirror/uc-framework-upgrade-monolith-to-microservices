package io.spring.article.infrastructure.mybatis.mapper;

import io.spring.article.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// MyBatis mapper interface for article favorite operations.
@Mapper
public interface ArticleFavoriteMapper {
  void insert(@Param("articleFavorite") ArticleFavorite articleFavorite);

  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  void delete(@Param("favorite") ArticleFavorite favorite);

  int countByArticleId(@Param("articleId") String articleId);
}
