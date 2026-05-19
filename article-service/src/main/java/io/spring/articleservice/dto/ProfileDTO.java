package io.spring.articleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO for user profile data received from user-service.
// Article-service does not own user data; it fetches profile info via REST.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
