package io.spring.article.api;

import io.spring.article.domain.Article;
import io.spring.article.domain.ArticleRepository;
import io.spring.article.domain.Tag;
import io.spring.article.dto.ArticleDto;
import io.spring.article.dto.NewArticleParam;
import io.spring.article.dto.UpdateArticleParam;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
public class ArticlesController {

  private final ArticleRepository articleRepository;

  public ArticlesController(ArticleRepository articleRepository) {
    this.articleRepository = articleRepository;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleParam param,
      @RequestHeader(value = "X-User-Id", required = false) String userId) {
    Article article =
        new Article(
            param.getTitle(),
            param.getDescription(),
            param.getBody(),
            param.getTagList() != null ? param.getTagList() : Collections.emptyList(),
            userId);
    articleRepository.save(article);
    return ResponseEntity.status(HttpStatus.CREATED).body(articleResponse(article));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> getArticle(@PathVariable String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(articleResponse(article)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{slug}")
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable String slug, @Valid @RequestBody UpdateArticleParam param) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              article.update(param.getTitle(), param.getDescription(), param.getBody());
              articleRepository.save(article);
              return ResponseEntity.ok(articleResponse(article));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(@PathVariable String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              articleRepository.remove(article);
              return ResponseEntity.noContent().<Void>build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> articleResponse(Article article) {
    Map<String, Object> response = new HashMap<>();
    ArticleDto dto =
        ArticleDto.builder()
            .id(article.getId())
            .slug(article.getSlug())
            .title(article.getTitle())
            .description(article.getDescription())
            .body(article.getBody())
            .tagList(article.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
            .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getCreatedAt()))
            .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(article.getUpdatedAt()))
            .build();
    response.put("article", dto);
    return response;
  }
}
