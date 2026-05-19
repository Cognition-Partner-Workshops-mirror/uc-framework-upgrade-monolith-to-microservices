package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DTO for creating a new comment (inbound request).
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("comment")
public class NewCommentDTO {
  @NotBlank(message = "can't be empty")
  private String body;
}
