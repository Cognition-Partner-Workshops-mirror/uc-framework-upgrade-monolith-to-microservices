package io.spring.article.core.comment;

import java.util.Optional;

// Repository interface for comment persistence operations
public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  void remove(Comment comment);
}
