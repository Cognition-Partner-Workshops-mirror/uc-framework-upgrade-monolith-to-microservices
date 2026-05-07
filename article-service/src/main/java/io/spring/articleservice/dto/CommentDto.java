package io.spring.articleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comment DTO for cross-service communication. Decouples the internal Comment domain model from the
 * API contract between article-service and user-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private String id;
  private String body;
  private String createdAt;
  private String updatedAt;
  private ProfileDto author;
}
