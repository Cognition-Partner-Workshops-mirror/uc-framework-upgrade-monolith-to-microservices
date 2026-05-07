package io.spring.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto {
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
