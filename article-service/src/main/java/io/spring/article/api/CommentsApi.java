package io.spring.article.api;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.comment.Comment;
import io.spring.article.core.comment.CommentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable("slug") String slug,
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody NewCommentParam param) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    Comment comment = new Comment(param.getBody(), userId, article.getId());
    commentRepository.save(comment);
    Map<String, Object> response = new HashMap<>();
    response.put("comment", Map.of("id", comment.getId(), "body", comment.getBody()));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("commentId") String commentId,
      @RequestHeader("X-User-Id") String userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    Comment comment =
        commentRepository
            .findById(article.getId(), commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
    commentRepository.remove(comment);
    return ResponseEntity.noContent().build();
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class NewCommentParam {
    @NotBlank private String body;
  }
}
