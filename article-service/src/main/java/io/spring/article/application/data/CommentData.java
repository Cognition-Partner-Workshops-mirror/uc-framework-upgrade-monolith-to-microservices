package io.spring.article.application.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

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
