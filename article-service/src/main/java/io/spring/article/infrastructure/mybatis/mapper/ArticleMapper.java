package io.spring.article.infrastructure.mybatis.mapper;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// MyBatis mapper interface for article CRUD operations.
@Mapper
public interface ArticleMapper {
  void insert(@Param("article") Article article);

  Article findById(@Param("id") String id);

  Article findBySlug(@Param("slug") String slug);

  Tag findTag(@Param("tagName") String tagName);

  void insertTag(@Param("tag") Tag tag);

  void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);

  void update(@Param("article") Article article);

  void delete(@Param("id") String id);
}
