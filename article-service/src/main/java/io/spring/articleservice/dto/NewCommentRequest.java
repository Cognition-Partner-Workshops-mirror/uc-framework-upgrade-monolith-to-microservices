package io.spring.articleservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating a new comment on an article. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;
}
