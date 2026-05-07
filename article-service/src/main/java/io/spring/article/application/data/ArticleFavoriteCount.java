package io.spring.article.application.data;

import lombok.Value;

// Immutable value object for article favorite count used in batch queries
@Value
public class ArticleFavoriteCount {
  private String id;
  private Integer count;
}
