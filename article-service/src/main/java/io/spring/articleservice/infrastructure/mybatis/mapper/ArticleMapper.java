package io.spring.articleservice.infrastructure.mybatis.mapper;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// MyBatis mapper for Article CRUD operations — maps to article-related SQL in ArticleMapper.xml
@Mapper
public interface ArticleMapper {
  Article findById(@Param("id") String id);

  Article findBySlug(@Param("slug") String slug);

  void insert(@Param("article") Article article);

  void update(@Param("article") Article article);

  void delete(@Param("id") String id);

  Tag findTag(@Param("tagName") String tagName);

  void insertTag(@Param("tag") Tag tag);

  void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);
}
