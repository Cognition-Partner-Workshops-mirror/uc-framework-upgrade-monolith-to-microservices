package io.spring.articleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for comment data transferred between services.
// Comments belong to the Article bounded context.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
  private String id;
  private String body;
  private String createdAt;
  private String updatedAt;
  private ProfileDTO author;
}
