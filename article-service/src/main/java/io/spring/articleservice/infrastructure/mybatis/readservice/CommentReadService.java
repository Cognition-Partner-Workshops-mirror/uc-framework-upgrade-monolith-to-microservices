package io.spring.articleservice.infrastructure.mybatis.readservice;

import io.spring.articleservice.core.comment.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

// Read-only MyBatis mapper for querying comments by article
@Mapper
public interface CommentReadService {
  List<Comment> findByArticleId(@Param("articleId") String articleId);
}
