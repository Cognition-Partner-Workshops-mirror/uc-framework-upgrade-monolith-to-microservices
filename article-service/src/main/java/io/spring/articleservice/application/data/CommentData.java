package io.spring.articleservice.application.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

// DTO for comment responses — includes author profile resolved from user-service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentData {
  private String id;
  private String body;
  private String articleId;
  private DateTime createdAt;
  private ProfileData profileData;
}
