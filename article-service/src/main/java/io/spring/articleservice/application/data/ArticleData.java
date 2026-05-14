package io.spring.articleservice.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

// DTO for article responses — includes author profile resolved from user-service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleData {
  private String id;
  private String slug;
  private String title;
  private String description;
  private String body;
  private boolean favorited;
  private int favoritesCount;
  private DateTime createdAt;
  private DateTime updatedAt;
  private List<String> tagList;

  @JsonProperty("author")
  private ProfileData profileData;
}
