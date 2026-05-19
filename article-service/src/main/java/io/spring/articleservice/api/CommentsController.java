package io.spring.articleservice.api;

import io.spring.articleservice.core.comment.Comment;
import io.spring.articleservice.dto.CommentDTO;
import io.spring.articleservice.dto.NewCommentDTO;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller for comment operations in the extracted microservice.
// Comments belong to the Article bounded context.
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
public class CommentsController {

  // In-memory comment store keyed by articleId (production would use MyBatis + SQLite)
  private final Map<String, List<Comment>> commentsByArticle = new ConcurrentHashMap<>();

  // POST /articles/{slug}/comments - Create a comment on an article
  @PostMapping
  public ResponseEntity<Map<String, Object>> createComment(
      @PathVariable("slug") String slug,
      @Valid @RequestBody NewCommentDTO newCommentDTO) {
    Comment comment = new Comment(newCommentDTO.getBody(), "default-user", slug);
    commentsByArticle.computeIfAbsent(slug, k -> new ArrayList<>()).add(comment);

    Map<String, Object> response = new HashMap<>();
    response.put("comment", toDTO(comment));
    return ResponseEntity.status(201).body(response);
  }

  // GET /articles/{slug}/comments - Get all comments for an article
  @GetMapping
  public ResponseEntity<Map<String, Object>> getComments(@PathVariable("slug") String slug) {
    List<CommentDTO> comments = commentsByArticle.getOrDefault(slug, new ArrayList<>()).stream()
        .map(this::toDTO)
        .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("comments", comments);
    return ResponseEntity.ok(response);
  }

  // DELETE /articles/{slug}/comments/{id} - Delete a comment
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId) {
    List<Comment> comments = commentsByArticle.get(slug);
    if (comments != null) {
      comments.removeIf(c -> c.getId().equals(commentId));
    }
    return ResponseEntity.noContent().build();
  }

  // Convert domain Comment to DTO
  private CommentDTO toDTO(Comment comment) {
    return CommentDTO.builder()
        .id(comment.getId())
        .body(comment.getBody())
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(comment.getCreatedAt()))
        .build();
  }
}
