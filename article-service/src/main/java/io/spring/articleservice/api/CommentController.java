package io.spring.articleservice.api;

import io.spring.articleservice.domain.article.Article;
import io.spring.articleservice.domain.article.ArticleRepository;
import io.spring.articleservice.domain.comment.Comment;
import io.spring.articleservice.domain.comment.CommentRepository;
import io.spring.articleservice.dto.CommentDto;
import io.spring.articleservice.dto.NewCommentRequest;
import io.spring.articleservice.dto.ProfileDto;
import io.spring.articleservice.exception.NoAuthorizationException;
import io.spring.articleservice.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Comment CRUD operations in the extracted article-service. Comments are scoped
 * to articles via the /api/articles/{slug}/comments path. User identity is passed via X-User-Id
 * header from the API gateway / user-service.
 */
@RestController
@RequestMapping("/api/articles/{slug}/comments")
@AllArgsConstructor
public class CommentController {

  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;

  /** Create a new comment on an article. */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable String slug,
      @RequestBody NewCommentRequest request,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(request.getBody(), userId, article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(wrapComment(toDto(comment)));
  }

  /** Delete a comment by ID. Only the comment author or article owner may delete. */
  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug,
      @PathVariable String commentId,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment =
        commentRepository
            .findById(article.getId(), commentId)
            .orElseThrow(ResourceNotFoundException::new);
    // Authorization: only article owner or comment author can delete
    if (userId == null
        || (!userId.equals(article.getUserId()) && !userId.equals(comment.getUserId()))) {
      throw new NoAuthorizationException();
    }
    commentRepository.remove(comment);
    return ResponseEntity.noContent().build();
  }

  /** Convert Comment domain entity to CommentDto for the API response. */
  private CommentDto toDto(Comment comment) {
    return CommentDto.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .author(ProfileDto.builder().id(comment.getUserId()).build())
        .build();
  }

  /** Wrap comment DTO in the standard {"comment": ...} envelope. */
  private Map<String, Object> wrapComment(CommentDto dto) {
    Map<String, Object> map = new HashMap<>();
    map.put("comment", dto);
    return map;
  }
}
