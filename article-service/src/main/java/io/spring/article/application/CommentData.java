package io.spring.article.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

// Read-model DTO for comment data, includes author profile from user-service.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {
  private String id;
  private String body;
  private String articleId;
  private DateTime createdAt;
  private ProfileData profileData;
}
