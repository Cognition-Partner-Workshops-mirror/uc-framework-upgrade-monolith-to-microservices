package io.spring.articleservice.core.comment;

import java.util.Optional;

// Repository interface for Comment persistence — extracted from the monolith
public interface CommentRepository {
  void save(Comment comment);

  Optional<Comment> findById(String articleId, String id);

  void remove(Comment comment);
}
