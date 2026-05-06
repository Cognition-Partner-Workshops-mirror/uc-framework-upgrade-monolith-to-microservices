package io.spring.article.infrastructure.mybatis.mapper;

import io.spring.article.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleFavoriteMapper {
  void insert(@Param("favorite") ArticleFavorite favorite);

  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  void delete(@Param("articleId") String articleId, @Param("userId") String userId);
}
