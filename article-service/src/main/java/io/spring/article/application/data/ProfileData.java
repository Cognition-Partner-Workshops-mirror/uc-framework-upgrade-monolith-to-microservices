package io.spring.article.application.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
