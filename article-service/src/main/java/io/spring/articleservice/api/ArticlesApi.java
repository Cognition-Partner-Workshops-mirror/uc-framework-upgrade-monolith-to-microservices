package io.spring.articleservice.api;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Articles API - handles article creation and listing
// Extracted from monolith ArticlesApi, uses userId (String) as principal
@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {

  private ArticleRepository articleRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam,
      @AuthenticationPrincipal String userId) {
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList() != null ? newArticleParam.getTagList() : List.of(),
            userId);
    articleRepository.save(article);
    return ResponseEntity.ok(
        new HashMap<>() {
          {
            put("article", articleToMap(article));
          }
        });
  }

  @GetMapping(path = "feed")
  public ResponseEntity<Map<String, Object>> getFeed(@AuthenticationPrincipal String userId) {
    // Simplified feed - in production, would query followed users via user-service
    return ResponseEntity.ok(Map.of("articles", List.of(), "articlesCount", 0));
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> getArticles() {
    // Simplified listing - returns empty list (full query service would be added in production)
    return ResponseEntity.ok(Map.of("articles", List.of(), "articlesCount", 0));
  }

  // Helper to convert Article entity to response map
  private Map<String, Object> articleToMap(Article article) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", article.getId());
    map.put("slug", article.getSlug());
    map.put("title", article.getTitle());
    map.put("description", article.getDescription());
    map.put("body", article.getBody());
    map.put("tagList", article.getTags().stream().map(Tag::getName).toList());
    map.put("createdAt", article.getCreatedAt().toString());
    map.put("updatedAt", article.getUpdatedAt().toString());
    return map;
  }

  @Getter
  @NoArgsConstructor
  static class NewArticleParam {
    @NotBlank private String title;
    @NotBlank private String description;
    @NotBlank private String body;
    private List<String> tagList;
  }
}
