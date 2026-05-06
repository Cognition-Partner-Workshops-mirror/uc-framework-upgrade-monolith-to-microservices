package io.spring.article.api;

import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.favorite.ArticleFavorite;
import io.spring.article.core.favorite.ArticleFavoriteRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{slug}/favorite")
@AllArgsConstructor
public class FavoritesApi {

  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @RequestHeader("X-User-Id") String userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(favorite);
    Map<String, Object> response = new HashMap<>();
    response.put("article", Map.of("slug", article.getSlug(), "favorited", true));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @RequestHeader("X-User-Id") String userId) {
    Article article =
        articleRepository
            .findBySlug(slug)
            .orElseThrow(() -> new RuntimeException("Article not found"));
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(articleFavoriteRepository::remove);
    Map<String, Object> response = new HashMap<>();
    response.put("article", Map.of("slug", article.getSlug(), "favorited", false));
    return ResponseEntity.ok(response);
  }
}
