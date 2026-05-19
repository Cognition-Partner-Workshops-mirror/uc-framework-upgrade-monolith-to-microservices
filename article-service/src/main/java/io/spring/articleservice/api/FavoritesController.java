package io.spring.articleservice.api;

import io.spring.articleservice.core.favorite.ArticleFavorite;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller for article favorite operations.
// Favorites are part of the Article bounded context, linking user IDs to article IDs.
@RestController
@RequestMapping(path = "/articles/{slug}/favorite")
public class FavoritesController {

  // In-memory favorites store (production would use MyBatis + SQLite)
  private final Map<String, Set<String>> favoritesByArticle = new ConcurrentHashMap<>();

  // POST /articles/{slug}/favorite - Favorite an article
  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug) {
    String userId = "default-user";
    favoritesByArticle
        .computeIfAbsent(slug, k -> ConcurrentHashMap.newKeySet())
        .add(userId);

    Map<String, Object> response = new HashMap<>();
    Map<String, Object> article = new HashMap<>();
    article.put("slug", slug);
    article.put("favorited", true);
    article.put("favoritesCount", favoritesByArticle.get(slug).size());
    response.put("article", article);
    return ResponseEntity.ok(response);
  }

  // DELETE /articles/{slug}/favorite - Unfavorite an article
  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug) {
    String userId = "default-user";
    Set<String> favorites = favoritesByArticle.get(slug);
    if (favorites != null) {
      favorites.remove(userId);
    }

    Map<String, Object> response = new HashMap<>();
    Map<String, Object> article = new HashMap<>();
    article.put("slug", slug);
    article.put("favorited", false);
    article.put("favoritesCount", favorites != null ? favorites.size() : 0);
    response.put("article", article);
    return ResponseEntity.ok(response);
  }
}
