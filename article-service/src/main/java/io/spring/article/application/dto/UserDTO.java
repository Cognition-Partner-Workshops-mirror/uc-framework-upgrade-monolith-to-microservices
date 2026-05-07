package io.spring.article.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO representing user data fetched from user-service via REST client
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
  private String id;
  private String username;
  private String bio;
  private String image;
}
