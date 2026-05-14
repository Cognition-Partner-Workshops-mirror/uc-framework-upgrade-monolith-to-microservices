package io.spring.articleservice.application.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO representing a user profile — resolved via REST call to user-service (monolith)
// This avoids a direct dependency on the User domain entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileData {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
