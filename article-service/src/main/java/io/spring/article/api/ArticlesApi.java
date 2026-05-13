package io.spring.article.api;

import io.spring.article.application.ArticleData;
import io.spring.article.application.ProfileData;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.article.Tag;
import io.spring.article.core.favorite.ArticleFavoriteRepository;
import io.spring.article.infrastructure.client.UserServiceClient;
import io.spring.article.infrastructure.mybatis.mapper.TagReadService;
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
import org.springframework.web.bind.annotation.*;

// Article CRUD REST controller — the primary API surface of the article-service.
// Communicates with user-service via REST client for author profile data.
@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticlesApi {

  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private UserServiceClient userServiceClient;
  private TagReadService tagReadService;

  // Create a new article. Requires userId header from the gateway/caller.
  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam,
      @RequestHeader("X-User-Id") String userId) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            userId);
    articleRepository.save(article);
    return ResponseEntity.ok(articleResponse(article, userId));
  }

  // Get a single article by slug.
  @GetMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> getArticle(
      @PathVariable String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(articleResponse(article, userId)))
        .orElse(ResponseEntity.notFound().build());
  }

  // Update an article (only owner can update).
  @PutMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable String slug,
      @RequestHeader("X-User-Id") String userId,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).<Map<String, Object>>build();
              }
              article.update(
                  updateArticleParam.getTitle(),
                  updateArticleParam.getDescription(),
                  updateArticleParam.getBody());
              articleRepository.save(article);
              return ResponseEntity.ok(articleResponse(article, userId));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Delete an article (only owner can delete).
  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(
      @PathVariable String slug, @RequestHeader("X-User-Id") String userId) {
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

  // Build article response JSON with author profile from user-service.
  private Map<String, Object> articleResponse(Article article, String currentUserId) {
    ProfileData author =
        userServiceClient
            .getProfileByUserId(article.getUserId())
            .orElse(new ProfileData(article.getUserId(), "unknown", "", "", false));

    int favoritesCount = articleFavoriteRepository.countByArticleId(article.getId());
    boolean favorited =
        currentUserId != null
            && articleFavoriteRepository.isFavorited(article.getId(), currentUserId);

    ArticleData articleData = new ArticleData();
    articleData.setId(article.getId());
    articleData.setSlug(article.getSlug());
    articleData.setTitle(article.getTitle());
    articleData.setDescription(article.getDescription());
    articleData.setBody(article.getBody());
    articleData.setFavorited(favorited);
    articleData.setFavoritesCount(favoritesCount);
    articleData.setCreatedAt(article.getCreatedAt());
    articleData.setUpdatedAt(article.getUpdatedAt());
    articleData.setTagList(
        article.getTags().stream().map(Tag::getName).collect(Collectors.toList()));
    articleData.setProfileData(author);

    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}

// DTO for article creation request
@Getter
@NoArgsConstructor
class NewArticleParam {
  @NotBlank(message = "can't be empty")
  private String title;

  @NotBlank(message = "can't be empty")
  private String description;

  @NotBlank(message = "can't be empty")
  private String body;

  private List<String> tagList = List.of();
}

// DTO for article update request
@Getter
@NoArgsConstructor
class UpdateArticleParam {
  private String title;
  private String description;
  private String body;
}
