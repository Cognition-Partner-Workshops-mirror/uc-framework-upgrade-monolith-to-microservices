package io.spring.articleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating an existing article via the article-service API. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArticleRequest {
  private String title;
  private String description;
  private String body;
}
