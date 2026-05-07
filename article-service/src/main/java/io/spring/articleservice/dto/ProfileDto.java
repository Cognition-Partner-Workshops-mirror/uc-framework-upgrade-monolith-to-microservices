package io.spring.articleservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Profile DTO for cross-service communication. Represents user profile data returned by the
 * user-service and embedded in article/comment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
