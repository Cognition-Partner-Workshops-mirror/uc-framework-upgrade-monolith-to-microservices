package io.spring.articleservice.api;

import io.spring.articleservice.domain.article.Article;
import io.spring.articleservice.domain.article.ArticleRepository;
import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.dto.NewArticleRequest;
import io.spring.articleservice.dto.ProfileDto;
import io.spring.articleservice.dto.UpdateArticleRequest;
import io.spring.articleservice.exception.NoAuthorizationException;
import io.spring.articleservice.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Article CRUD operations in the extracted article-service. Handles creation,
 * retrieval, update, and deletion of articles. User identity is passed via X-User-Id header from
 * the API gateway / user-service.
 */
@RestController
@RequestMapping("/api/articles")
@AllArgsConstructor
public class ArticleController {

  private final ArticleRepository articleRepository;

  /** Create a new article. User ID comes from the X-User-Id header set by the gateway. */
  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @RequestBody NewArticleRequest request,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        new Article(
            request.getTitle(),
            request.getDescription(),
            request.getBody(),
            request.getTagList() != null ? request.getTagList() : List.of(),
            userId);
    articleRepository.save(article);
    return ResponseEntity.ok(wrapArticle(toDto(article)));
  }

  /** Get a single article by slug. */
  @GetMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> getArticle(@PathVariable String slug) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(wrapArticle(toDto(article)));
  }

  /** Update an existing article. Only the article owner may update. */
  @PutMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable String slug,
      @RequestBody UpdateArticleRequest request,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (userId == null || !userId.equals(article.getUserId())) {
      throw new NoAuthorizationException();
    }
    article.update(request.getTitle(), request.getDescription(), request.getBody());
    articleRepository.save(article);
    return ResponseEntity.ok(wrapArticle(toDto(article)));
  }

  /** Delete an article by slug. Only the article owner may delete. */
  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (userId == null || !userId.equals(article.getUserId())) {
      throw new NoAuthorizationException();
    }
    articleRepository.remove(article);
    return ResponseEntity.noContent().build();
  }

  /** Convert Article domain entity to ArticleDto for the API response. */
  private ArticleDto toDto(Article article) {
    return ArticleDto.builder()
        .id(article.getId())
        .slug(article.getSlug())
        .title(article.getTitle())
        .description(article.getDescription())
        .body(article.getBody())
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getCreatedAt()))
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getUpdatedAt()))
        .tagList(article.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()))
        .author(ProfileDto.builder().id(article.getUserId()).build())
        .build();
  }

  /** Wrap article DTO in the standard {"article": ...} envelope. */
  private Map<String, Object> wrapArticle(ArticleDto dto) {
    Map<String, Object> map = new HashMap<>();
    map.put("article", dto);
    return map;
  }
}
