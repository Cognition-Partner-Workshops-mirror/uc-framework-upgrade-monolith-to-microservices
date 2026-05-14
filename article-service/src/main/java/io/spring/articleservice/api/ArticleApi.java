package io.spring.articleservice.api;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.application.data.ProfileData;
import io.spring.articleservice.client.UserServiceClient;
import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Article CRUD controller — extracted from the monolith's ArticleApi
// Handles create, read, update, delete operations for individual articles
@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticleApi {

  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private UserServiceClient userServiceClient;

  // Create a new article — requires authentication
  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleParam param) {
    String userId = getCurrentUserId();
    if (userId == null) {
      return ResponseEntity.status(401).build();
    }
    Article article =
        new Article(param.getTitle(), param.getDescription(), param.getBody(), param.getTagList(), userId);
    articleRepository.save(article);
    return ResponseEntity.ok(articleResponse(article, userId));
  }

  // Get a single article by slug — public endpoint
  @GetMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> getArticle(@PathVariable String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(articleResponse(article, getCurrentUserId())))
        .orElse(ResponseEntity.notFound().build());
  }

  // Update an existing article — only the author can update
  @PutMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable String slug, @RequestBody UpdateArticleParam param) {
    String userId = getCurrentUserId();
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).<Map<String, Object>>build();
              }
              article.update(param.getTitle(), param.getDescription(), param.getBody());
              articleRepository.save(article);
              return ResponseEntity.ok(articleResponse(article, userId));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Delete an article — only the author can delete
  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(@PathVariable String slug) {
    String userId = getCurrentUserId();
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).<Void>build();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Favorite an article — creates an ArticleFavorite record
  @PostMapping("/{slug}/favorite")
  public ResponseEntity<Map<String, Object>> favoriteArticle(@PathVariable String slug) {
    String userId = getCurrentUserId();
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleFavorite fav = new ArticleFavorite(article.getId(), userId);
              articleFavoriteRepository.save(fav);
              return ResponseEntity.ok(articleResponse(article, userId));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Unfavorite an article — removes the ArticleFavorite record
  @DeleteMapping("/{slug}/favorite")
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(@PathVariable String slug) {
    String userId = getCurrentUserId();
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              articleFavoriteRepository
                  .find(article.getId(), userId)
                  .ifPresent(articleFavoriteRepository::remove);
              return ResponseEntity.ok(articleResponse(article, userId));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Build the article response map with author profile from user-service
  private Map<String, Object> articleResponse(Article article, String currentUserId) {
    ProfileData author =
        userServiceClient
            .getProfileByUserId(article.getUserId())
            .orElse(ProfileData.builder().id(article.getUserId()).username("unknown").build());

    boolean favorited =
        currentUserId != null
            && articleFavoriteRepository.find(article.getId(), currentUserId).isPresent();

    ArticleData data =
        ArticleData.builder()
            .id(article.getId())
            .slug(article.getSlug())
            .title(article.getTitle())
            .description(article.getDescription())
            .body(article.getBody())
            .tagList(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
            .favorited(favorited)
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .profileData(author)
            .build();

    Map<String, Object> response = new HashMap<>();
    response.put("article", data);
    return response;
  }

  // Extract the current authenticated user ID from the security context
  private String getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof String) {
      return (String) auth.getPrincipal();
    }
    return null;
  }

  // Request body for creating a new article
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class NewArticleParam {
    @NotBlank private String title;
    @NotBlank private String description;
    @NotBlank private String body;
    private List<String> tagList;
  }

  // Request body for updating an article
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class UpdateArticleParam {
    private String title;
    private String description;
    private String body;
  }
}
