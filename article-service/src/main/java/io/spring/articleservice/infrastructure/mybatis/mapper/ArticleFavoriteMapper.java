package io.spring.articleservice.infrastructure.mybatis.mapper;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// MyBatis mapper for ArticleFavorite operations
@Mapper
public interface ArticleFavoriteMapper {
  ArticleFavorite find(@Param("articleId") String articleId, @Param("userId") String userId);

  void insert(@Param("favorite") ArticleFavorite favorite);

  void delete(@Param("favorite") ArticleFavorite favorite);
}
