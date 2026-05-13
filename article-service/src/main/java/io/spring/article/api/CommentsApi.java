package io.spring.article.api;

import io.spring.article.application.CommentData;
import io.spring.article.application.ProfileData;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.comment.Comment;
import io.spring.article.core.comment.CommentRepository;
import io.spring.article.infrastructure.client.UserServiceClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Comments REST controller — manages comments on articles.
// Part of the Article bounded context, communicates with user-service for commenter profiles.
@RestController
@RequestMapping("/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private UserServiceClient userServiceClient;

  // Create a comment on an article.
  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable String slug,
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Comment comment = new Comment(newCommentParam.getBody(), userId, article.getId());
              commentRepository.save(comment);
              return ResponseEntity.ok(commentResponse(comment));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Delete a comment (only the comment author or article owner can delete).
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug,
      @PathVariable String commentId,
      @RequestHeader("X-User-Id") String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                commentRepository
                    .findById(article.getId(), commentId)
                    .map(
                        comment -> {
                          // Only comment author or article owner can delete
                          if (!comment.getUserId().equals(userId)
                              && !article.getUserId().equals(userId)) {
                            return ResponseEntity.status(403).<Void>build();
                          }
                          commentRepository.remove(comment);
                          return ResponseEntity.noContent().<Void>build();
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }

  // Build comment response JSON with author profile from user-service.
  private Map<String, Object> commentResponse(Comment comment) {
    ProfileData author =
        userServiceClient
            .getProfileByUserId(comment.getUserId())
            .orElse(new ProfileData(comment.getUserId(), "unknown", "", "", false));

    CommentData commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            author);

    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

// DTO for comment creation request
@Getter
@NoArgsConstructor
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
