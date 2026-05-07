package io.spring.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
  private String id;
  private String body;
  private String createdAt;
  private String updatedAt;
  private AuthorDto author;
}
