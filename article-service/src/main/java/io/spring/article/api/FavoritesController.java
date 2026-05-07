package io.spring.article.api;

import io.spring.article.domain.ArticleFavorite;
import io.spring.article.domain.ArticleFavoriteRepository;
import io.spring.article.domain.ArticleRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{slug}/favorite")
public class FavoritesController {

  private final ArticleRepository articleRepository;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  public FavoritesController(
      ArticleRepository articleRepository,
      ArticleFavoriteRepository articleFavoriteRepository) {
    this.articleRepository = articleRepository;
    this.articleFavoriteRepository = articleFavoriteRepository;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleFavorite fav = new ArticleFavorite(article.getId(), userId);
              articleFavoriteRepository.save(fav);
              Map<String, Object> response = new HashMap<>();
              response.put("article", Map.of("slug", article.getSlug(), "favorited", true));
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              articleFavoriteRepository
                  .find(article.getId(), userId)
                  .ifPresent(articleFavoriteRepository::remove);
              Map<String, Object> response = new HashMap<>();
              response.put("article", Map.of("slug", article.getSlug(), "favorited", false));
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }
}
