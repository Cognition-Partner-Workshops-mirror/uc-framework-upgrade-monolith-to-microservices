package io.spring.article.api;

import io.spring.article.application.data.ArticleData;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@AllArgsConstructor
public class ArticlesApi {

  private ArticleRepository articleRepository;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @RequestHeader("X-User-Id") String userId, @Valid @RequestBody NewArticleParam param) {
    Article article =
        new Article(
            param.getTitle(), param.getDescription(), param.getBody(), param.getTagList(), userId);
    articleRepository.save(article);
    Map<String, Object> response = new HashMap<>();
    response.put("article", toArticleData(article));
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> listArticles(
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @RequestParam(value = "favorited", required = false) String favorited,
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit) {
    Map<String, Object> response = new HashMap<>();
    response.put("articles", List.of());
    response.put("articlesCount", 0);
    return ResponseEntity.ok(response);
  }

  private ArticleData toArticleData(Article article) {
    return ArticleData.builder()
        .id(article.getId())
        .slug(article.getSlug())
        .title(article.getTitle())
        .description(article.getDescription())
        .body(article.getBody())
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .build();
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  static class NewArticleParam {
    @NotBlank private String title;
    @NotBlank private String description;
    @NotBlank private String body;
    private List<String> tagList;
  }
}
