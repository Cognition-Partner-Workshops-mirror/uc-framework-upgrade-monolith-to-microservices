package io.spring.article.application;

import io.spring.article.application.client.UserServiceClient;
import io.spring.article.application.data.CommentData;
import io.spring.article.infrastructure.mybatis.readservice.CommentReadService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Query service for comments. Uses UserServiceClient instead of direct
 * UserRelationshipQueryService to fetch follow relationships from user-service.
 */
@Service
@AllArgsConstructor
public class CommentQueryService {
  private CommentReadService commentReadService;
  private UserServiceClient userServiceClient;

  public Optional<CommentData> findById(String id, String userId) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return Optional.empty();
    } else {
      // Cross-service call to check if current user follows comment author
      if (userId != null) {
        try {
          commentData
              .getProfileData()
              .setFollowing(
                  userServiceClient.isFollowing(userId, commentData.getProfileData().getId()));
        } catch (Exception e) {
          // Graceful degradation: leave following as false
        }
      }
    }
    return Optional.ofNullable(commentData);
  }

  public List<CommentData> findByArticleId(String articleId, String userId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    if (comments.size() > 0 && userId != null) {
      // Cross-service call to check follow relationships for all comment authors
      try {
        Set<String> followingAuthors =
            userServiceClient.getFollowingUsers(
                userId,
                comments.stream()
                    .map(c -> c.getProfileData().getId())
                    .collect(Collectors.toList()));
        comments.forEach(
            commentData -> {
              if (followingAuthors.contains(commentData.getProfileData().getId())) {
                commentData.getProfileData().setFollowing(true);
              }
            });
      } catch (Exception e) {
        // Graceful degradation: leave following as false
      }
    }
    return comments;
  }
}
