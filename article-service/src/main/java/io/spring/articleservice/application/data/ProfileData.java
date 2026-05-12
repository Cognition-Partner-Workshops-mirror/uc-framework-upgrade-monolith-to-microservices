package io.spring.articleservice.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ProfileData DTO - received from user-service via REST client
// Contains user profile information needed for article author display
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
