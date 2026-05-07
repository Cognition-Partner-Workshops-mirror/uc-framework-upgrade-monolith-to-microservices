package io.spring.article.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Read-model DTO for user profile information attached to articles and comments
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
  @JsonIgnore private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
