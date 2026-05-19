package io.spring.articleservice.api;

import io.spring.articleservice.core.article.Article;
import io.spring.articleservice.core.article.Tag;
import io.spring.articleservice.dto.ArticleDTO;
import io.spring.articleservice.dto.NewArticleDTO;
import io.spring.articleservice.dto.ProfileDTO;
import io.spring.articleservice.client.UserServiceClient;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST controller for article operations in the extracted microservice.
// Mirrors the monolith's ArticlesApi and ArticleApi endpoints for API compatibility.
// Uses in-memory storage for demonstration; production would use MyBatis/SQLite.
@RestController
@RequestMapping(path = "/articles")
public class ArticlesController {

  // In-memory article store (production would use MyBatis + SQLite)
  private final Map<String, Article> articlesBySlug = new ConcurrentHashMap<>();
  private final Map<String, Article> articlesById = new ConcurrentHashMap<>();
  private final UserServiceClient userServiceClient;

  public ArticlesController(UserServiceClient userServiceClient) {
    this.userServiceClient = userServiceClient;
  }

  // POST /articles - Create a new article
  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleDTO newArticleDTO) {
    List<String> tagList = newArticleDTO.getTagList() != null
        ? newArticleDTO.getTagList() : new ArrayList<>();
    Article article = new Article(
        newArticleDTO.getTitle(),
        newArticleDTO.getDescription(),
        newArticleDTO.getBody(),
        tagList,
        "default-user");
    articlesBySlug.put(article.getSlug(), article);
    articlesById.put(article.getId(), article);

    return ResponseEntity.ok(wrapArticle(toDTO(article)));
  }

  // GET /articles - List articles with optional filters
  @GetMapping
  public ResponseEntity<Map<String, Object>> getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @RequestParam(value = "favorited", required = false) String favoritedBy) {
    List<ArticleDTO> articles = articlesBySlug.values().stream()
        .map(this::toDTO)
        .skip(offset)
        .limit(limit)
        .collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("articles", articles);
    response.put("articlesCount", articlesBySlug.size());
    return ResponseEntity.ok(response);
  }

  // GET /articles/{slug} - Get a single article by slug
  @GetMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> getArticle(@PathVariable("slug") String slug) {
    Article article = articlesBySlug.get(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(wrapArticle(toDTO(article)));
  }

  // PUT /articles/{slug} - Update an existing article
  @PutMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable("slug") String slug,
      @RequestBody Map<String, Map<String, String>> updateParam) {
    Article article = articlesBySlug.get(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    Map<String, String> articleUpdate = updateParam.get("article");
    if (articleUpdate != null) {
      article.update(
          articleUpdate.get("title"),
          articleUpdate.get("description"),
          articleUpdate.get("body"));
      // Re-index by new slug if title changed
      articlesBySlug.remove(slug);
      articlesBySlug.put(article.getSlug(), article);
    }
    return ResponseEntity.ok(wrapArticle(toDTO(article)));
  }

  // DELETE /articles/{slug} - Delete an article
  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(@PathVariable("slug") String slug) {
    Article article = articlesBySlug.remove(slug);
    if (article != null) {
      articlesById.remove(article.getId());
    }
    return ResponseEntity.noContent().build();
  }

  // GET /tags - Get all tags
  @GetMapping("/tags")
  public ResponseEntity<Map<String, Object>> getTags() {
    List<String> tags = articlesBySlug.values().stream()
        .flatMap(a -> a.getTags().stream())
        .map(Tag::getName)
        .distinct()
        .collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tags);
    return ResponseEntity.ok(response);
  }

  // Convert domain Article to DTO
  private ArticleDTO toDTO(Article article) {
    return ArticleDTO.builder()
        .id(article.getId())
        .slug(article.getSlug())
        .title(article.getTitle())
        .description(article.getDescription())
        .body(article.getBody())
        .tagList(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
        .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getCreatedAt()))
        .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getUpdatedAt()))
        .favorited(false)
        .favoritesCount(0)
        .build();
  }

  private Map<String, Object> wrapArticle(ArticleDTO article) {
    Map<String, Object> response = new HashMap<>();
    response.put("article", article);
    return response;
  }
}
