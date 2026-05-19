package io.spring.articleservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for article data transferred between article-service and user-service.
// Decouples the Article domain entity from external API contracts.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private List<String> tagList;
  private String createdAt;
  private String updatedAt;
  private boolean favorited;
  private int favoritesCount;
  private ProfileDTO author;
}
