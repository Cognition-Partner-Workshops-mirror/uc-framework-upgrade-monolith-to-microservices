package io.spring.article.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewArticleParam {
  @NotBlank private String title;
  @NotBlank private String description;
  @NotBlank private String body;
  private List<String> tagList;
}
