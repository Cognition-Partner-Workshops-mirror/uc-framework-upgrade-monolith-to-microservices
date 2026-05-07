package io.spring.article.api;

import io.spring.article.domain.ArticleRepository;
import io.spring.article.domain.Comment;
import io.spring.article.domain.CommentRepository;
import io.spring.article.dto.CommentDto;
import io.spring.article.dto.NewCommentParam;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{slug}/comments")
public class CommentsController {

  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;

  public CommentsController(
      ArticleRepository articleRepository, CommentRepository commentRepository) {
    this.articleRepository = articleRepository;
    this.commentRepository = commentRepository;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> addComment(
      @PathVariable String slug,
      @Valid @RequestBody NewCommentParam param,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Comment comment = new Comment(param.getBody(), userId, article.getId());
              commentRepository.save(comment);
              Map<String, Object> response = new HashMap<>();
              response.put("comment", toCommentDto(comment));
              return ResponseEntity.status(HttpStatus.CREATED).body(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<Map<String, Object>> getComment(
      @PathVariable String slug, @PathVariable String commentId) {
    return articleRepository
        .findBySlug(slug)
        .flatMap(article -> commentRepository.findById(article.getId(), commentId))
        .map(
            comment -> {
              Map<String, Object> response = new HashMap<>();
              response.put("comment", toCommentDto(comment));
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String slug, @PathVariable String commentId) {
    return articleRepository
        .findBySlug(slug)
        .flatMap(article -> commentRepository.findById(article.getId(), commentId))
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private CommentDto toCommentDto(Comment comment) {
    return CommentDto.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .build();
  }
}
