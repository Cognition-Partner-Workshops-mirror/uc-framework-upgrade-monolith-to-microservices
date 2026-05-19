package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DTO for creating a new article (inbound request).
// Maps to the NewArticleParam in the monolith.
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("article")
public class NewArticleDTO {
  @NotBlank(message = "can't be empty")
  private String title;

  @NotBlank(message = "can't be empty")
  private String description;

  @NotBlank(message = "can't be empty")
  private String body;

  private List<String> tagList;
}
