package io.spring.article.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
  private List<String> tagList;
  private String createdAt;
  private String updatedAt;
  private boolean favorited;
  private int favoritesCount;
  private AuthorDto author;
}
