package io.spring.article.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Request body for updating an existing article
@Getter
@NoArgsConstructor
@JsonRootName("article")
public class UpdateArticleParam {
  private String title;
  private String description;
  private String body;
}
