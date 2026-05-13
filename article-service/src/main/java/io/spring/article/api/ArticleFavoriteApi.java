package io.spring.article.api;

import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.favorite.ArticleFavorite;
import io.spring.article.core.favorite.ArticleFavoriteRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Favorite/unfavorite REST controller — manages article favorites.
// Part of the Article bounded context.
@RestController
@RequestMapping("/articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {

  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;

  // Favorite an article.
  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable String slug, @RequestHeader("X-User-Id") String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
              articleFavoriteRepository.save(favorite);
              int count = articleFavoriteRepository.countByArticleId(article.getId());
              Map<String, Object> response = new HashMap<>();
              response.put("favoritesCount", count);
              response.put("favorited", true);
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Unfavorite an article.
  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable String slug, @RequestHeader("X-User-Id") String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                articleFavoriteRepository
                    .find(article.getId(), userId)
                    .map(
                        favorite -> {
                          articleFavoriteRepository.remove(favorite);
                          int count =
                              articleFavoriteRepository.countByArticleId(article.getId());
                          Map<String, Object> response = new HashMap<>();
                          response.put("favoritesCount", count);
                          response.put("favorited", false);
                          return ResponseEntity.ok(response);
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }
}
