package io.spring.article.application.client;

import io.spring.article.application.dto.ProfileDTO;
import io.spring.article.application.dto.UserDTO;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * REST client for cross-service communication with user-service.
 * Uses Spring Boot 3.2+ RestClient to call internal user-service endpoints
 * for user profile lookups and follow-relationship checks.
 */
@Component
public class UserServiceClient {

  private final RestClient restClient;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    // Build a RestClient targeting the user-service base URL
    this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
  }

  /** Fetch a single user by ID from user-service */
  public UserDTO getUserById(String userId) {
    return restClient
        .get()
        .uri("/api/internal/users/{id}", userId)
        .retrieve()
        .body(UserDTO.class);
  }

  /** Check if currentUser is following targetUser via user-service */
  public boolean isFollowing(String currentUserId, String targetUserId) {
    Boolean result =
        restClient
            .get()
            .uri(
                "/api/internal/users/{id}/is-following/{targetId}",
                currentUserId,
                targetUserId)
            .retrieve()
            .body(Boolean.class);
    return result != null && result;
  }

  /** Get the set of user IDs that currentUser follows from a given list */
  public Set<String> getFollowingUsers(String currentUserId, List<String> targetIds) {
    if (targetIds == null || targetIds.isEmpty()) {
      return Collections.emptySet();
    }
    String ids = targetIds.stream().collect(Collectors.joining(","));
    Set<String> result =
        restClient
            .get()
            .uri(
                "/api/internal/users/{id}/following?targetIds={targetIds}",
                currentUserId,
                ids)
            .retrieve()
            .body(new ParameterizedTypeReference<Set<String>>() {});
    return result != null ? result : Collections.emptySet();
  }
}
