package io.spring.article.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Request body for creating a new article
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("article")
public class NewArticleParam {
  @NotBlank(message = "can't be empty")
  private String title;

  @NotBlank(message = "can't be empty")
  private String description;

  @NotBlank(message = "can't be empty")
  private String body;

  private List<String> tagList;
}
