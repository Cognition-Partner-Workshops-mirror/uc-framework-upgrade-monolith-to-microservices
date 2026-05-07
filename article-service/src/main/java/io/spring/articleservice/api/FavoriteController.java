package io.spring.articleservice.api;

import io.spring.articleservice.domain.article.Article;
import io.spring.articleservice.domain.article.ArticleRepository;
import io.spring.articleservice.domain.favorite.ArticleFavorite;
import io.spring.articleservice.domain.favorite.ArticleFavoriteRepository;
import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.dto.ProfileDto;
import io.spring.articleservice.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Article Favorite operations in the extracted article-service. Handles
 * favoriting and unfavoriting articles for authenticated users. User identity is passed via
 * X-User-Id header from the API gateway / user-service.
 */
@RestController
@RequestMapping("/api/articles/{slug}/favorite")
@AllArgsConstructor
public class FavoriteController {

  private final ArticleRepository articleRepository;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  /** Favorite an article. */
  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(favorite);
    ArticleDto dto = toDto(article);
    dto.setFavorited(true);
    return ResponseEntity.ok(wrapArticle(dto));
  }

  /** Unfavorite an article. */
  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.ok(wrapArticle(toDto(article)));
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
