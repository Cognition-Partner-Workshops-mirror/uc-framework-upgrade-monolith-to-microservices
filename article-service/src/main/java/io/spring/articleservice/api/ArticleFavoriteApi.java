package io.spring.articleservice.api;

import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Article Favorite API - handles favorite/unfavorite operations
// Extracted from monolith ArticleFavoriteApi
@RestController
@RequestMapping(path = "/articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {

  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
              articleFavoriteRepository.save(favorite);
              Map<String, Object> result = new HashMap<>();
              Map<String, Object> articleMap = new HashMap<>();
              articleMap.put("slug", article.getSlug());
              articleMap.put("title", article.getTitle());
              articleMap.put("favorited", true);
              articleMap.put("tagList", article.getTags().stream().map(Tag::getName).toList());
              result.put("article", articleMap);
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                articleFavoriteRepository
                    .find(article.getId(), userId)
                    .map(
                        favorite -> {
                          articleFavoriteRepository.remove(favorite);
                          Map<String, Object> result = new HashMap<>();
                          Map<String, Object> articleMap = new HashMap<>();
                          articleMap.put("slug", article.getSlug());
                          articleMap.put("title", article.getTitle());
                          articleMap.put("favorited", false);
                          articleMap.put(
                              "tagList", article.getTags().stream().map(Tag::getName).toList());
                          result.put("article", articleMap);
                          return ResponseEntity.ok(result);
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }
}
