package io.spring.articleservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Article DTO for cross-service communication between article-service and user-service. Decouples
 * the internal Article domain model from the API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private boolean favorited;
  private int favoritesCount;
  private String createdAt;
  private String updatedAt;
  private List<String> tagList;
  private ProfileDto author;
}
