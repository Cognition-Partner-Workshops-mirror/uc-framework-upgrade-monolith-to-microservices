package io.spring.articleservice.api;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.article.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Single Article API - handles get, update, delete by slug
// Extracted from monolith ArticleApi
@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {

  private ArticleRepository articleRepository;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getArticle(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(wrapArticle(article)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable("slug") String slug,
      @RequestBody UpdateArticleParam updateArticleParam,
      @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              // Only the author can update their article
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).<Map<String, Object>>build();
              }
              article.update(
                  updateArticleParam.getTitle(),
                  updateArticleParam.getDescription(),
                  updateArticleParam.getBody());
              articleRepository.save(article);
              return ResponseEntity.ok(wrapArticle(article));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
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

  private Map<String, Object> wrapArticle(Article article) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> articleMap = new HashMap<>();
    articleMap.put("id", article.getId());
    articleMap.put("slug", article.getSlug());
    articleMap.put("title", article.getTitle());
    articleMap.put("description", article.getDescription());
    articleMap.put("body", article.getBody());
    articleMap.put("tagList", article.getTags().stream().map(Tag::getName).toList());
    articleMap.put("createdAt", article.getCreatedAt().toString());
    articleMap.put("updatedAt", article.getUpdatedAt().toString());
    map.put("article", articleMap);
    return map;
  }

  @Getter
  @NoArgsConstructor
  static class UpdateArticleParam {
    private String title;
    private String description;
    private String body;
  }
}
