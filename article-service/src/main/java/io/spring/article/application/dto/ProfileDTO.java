package io.spring.article.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO representing a user profile with follow status, fetched from user-service
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
