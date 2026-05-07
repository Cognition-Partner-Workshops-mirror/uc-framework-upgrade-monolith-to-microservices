package io.spring.articleservice.domain.comment;

import java.util.Optional;

/** Comment repository interface — extracted from io.spring.core.comment.CommentRepository. */
public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  void remove(Comment comment);
}
