package io.spring.articleservice.api;

import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.comment.Comment;
import io.spring.articleservice.core.comment.CommentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Comments API - handles comment creation and deletion
// Extracted from monolith CommentsApi
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> addComment(
      @PathVariable("slug") String slug,
      @Valid @RequestBody NewCommentParam newCommentParam,
      @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Comment comment = new Comment(newCommentParam.getBody(), userId, article.getId());
              commentRepository.save(comment);
              Map<String, Object> result = new HashMap<>();
              Map<String, Object> commentMap = new HashMap<>();
              commentMap.put("id", comment.getId());
              commentMap.put("body", comment.getBody());
              commentMap.put("createdAt", comment.getCreatedAt().toString());
              commentMap.put("updatedAt", comment.getCreatedAt().toString());
              result.put("comment", commentMap);
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                commentRepository
                    .findById(article.getId(), commentId)
                    .map(
                        comment -> {
                          // Only the comment author can delete
                          if (!comment.getUserId().equals(userId)) {
                            return ResponseEntity.status(403).<Void>build();
                          }
                          commentRepository.remove(comment);
                          return ResponseEntity.noContent().<Void>build();
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }

  @Getter
  @NoArgsConstructor
  static class NewCommentParam {
    @NotBlank private String body;
  }
}
