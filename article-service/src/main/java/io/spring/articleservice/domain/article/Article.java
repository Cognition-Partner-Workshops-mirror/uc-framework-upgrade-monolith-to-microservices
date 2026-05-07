package io.spring.articleservice.domain.article;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * Article aggregate root — extracted from io.spring.core.article.Article in the monolith. Owns
 * title, slug, body, description, tags, and timestamps.
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class Article {
  private String userId;
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private List<Tag> tags;
  private DateTime createdAt;
  private DateTime updatedAt;

  public Article(
      String title, String description, String body, List<String> tagList, String userId) {
    this(title, description, body, tagList, userId, new DateTime());
  }

  public Article(
      String title,
      String description,
      String body,
      List<String> tagList,
      String userId,
      DateTime createdAt) {
    this.id = UUID.randomUUID().toString();
    this.slug = toSlug(title);
    this.title = title;
    this.description = description;
    this.body = body;
    this.tags = new HashSet<>(tagList).stream().map(Tag::new).collect(Collectors.toList());
    this.userId = userId;
    this.createdAt = createdAt;
    this.updatedAt = createdAt;
  }

  /** Update mutable fields; only non-empty values are applied. */
  public void update(String title, String description, String body) {
    if (title != null && !title.isEmpty()) {
      this.title = title;
      this.slug = toSlug(title);
      this.updatedAt = new DateTime();
    }
    if (description != null && !description.isEmpty()) {
      this.description = description;
      this.updatedAt = new DateTime();
    }
    if (body != null && !body.isEmpty()) {
      this.body = body;
      this.updatedAt = new DateTime();
    }
  }

  /** Generate URL-friendly slug from title. */
  public static String toSlug(String title) {
    return title.toLowerCase().replaceAll("[\\&|[\\uFE30-\\uFFA0]|\\'|\\\"\\s\\?\\,\\.]+", "-");
  }
}
