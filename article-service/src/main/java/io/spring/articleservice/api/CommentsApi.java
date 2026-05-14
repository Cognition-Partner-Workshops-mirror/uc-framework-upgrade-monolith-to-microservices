package io.spring.articleservice.api;

import io.spring.articleservice.application.data.CommentData;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.client.UserServiceClient;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.comment.Comment;
import io.spring.articleservice.core.comment.CommentRepository;
import io.spring.articleservice.infrastructure.mybatis.readservice.CommentReadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Comments API — handles CRUD operations for comments on articles
// Comments belong to the Article bounded context
@RestController
@RequestMapping("/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  // Add a comment to an article — requires authentication
  @PostMapping
  public ResponseEntity<Map<String, Object>> addComment(
      @PathVariable String slug, @Valid @RequestBody NewCommentParam param) {
    String userId = getCurrentUserId();
    if (userId == null) {
      return ResponseEntity.status(401).build();
    }
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Comment comment = new Comment(param.getBody(), userId, article.getId());
              commentRepository.save(comment);
              Map<String, Object> response = new HashMap<>();
              response.put("comment", toCommentData(comment));
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // List all comments for an article — public endpoint
  @GetMapping
  public ResponseEntity<Map<String, Object>> getComments(@PathVariable String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              List<Comment> comments = commentReadService.findByArticleId(article.getId());
              List<CommentData> commentDataList =
                  comments.stream().map(this::toCommentData).collect(Collectors.toList());
              Map<String, Object> response = new HashMap<>();
              response.put("comments", commentDataList);
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Delete a comment — only the comment author can delete
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug, @PathVariable String commentId) {
    String userId = getCurrentUserId();
    return articleRepository
        .findBySlug(slug)
        .flatMap(article -> commentRepository.findById(article.getId(), commentId))
        .map(
            comment -> {
              if (!comment.getUserId().equals(userId)) {
                return ResponseEntity.status(403).<Void>build();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Convert Comment entity to CommentData DTO with author profile from user-service
  private CommentData toCommentData(Comment comment) {
    ProfileData author =
        userServiceClient
            .getProfileByUserId(comment.getUserId())
            .orElse(ProfileData.builder().id(comment.getUserId()).username("unknown").build());
    return CommentData.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .articleId(comment.getArticleId())
        .createdAt(comment.getCreatedAt())
        .profileData(author)
        .build();
  }

  private String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof String) {
      return (String) auth.getPrincipal();
    }
    return null;
  }

  // Request body for adding a new comment
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class NewCommentParam {
    @NotBlank private String body;
  }
}
