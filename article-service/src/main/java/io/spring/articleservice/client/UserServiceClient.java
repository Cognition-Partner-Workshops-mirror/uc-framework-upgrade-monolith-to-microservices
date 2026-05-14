package io.spring.articleservice.client;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// REST client for cross-service communication with user-service (monolith)
// Resolves user profiles by ID for article/comment author display
@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.userServiceUrl = userServiceUrl;
  }

  // Fetch a user profile by user ID from the user-service
  // Returns a default profile if the user-service is unavailable (graceful degradation)
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      String url = userServiceUrl + "/api/internal/users/" + userId;
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null) {
        return Optional.of(
            ProfileData.builder()
                .id((String) response.get("id"))
                .username((String) response.get("username"))
                .bio((String) response.get("bio"))
                .image((String) response.get("image"))
                .build());
      }
    } catch (Exception e) {
      // Graceful degradation — return empty if user-service is unavailable
      logger.warn("Failed to fetch profile for userId={}: {}", userId, e.getMessage());
    }
    return Optional.empty();
  }

  // Fetch a user profile by username from the user-service
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfileByUsername(String username) {
    try {
      String url = userServiceUrl + "/api/internal/users/by-username/" + username;
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null) {
        return Optional.of(
            ProfileData.builder()
                .id((String) response.get("id"))
                .username((String) response.get("username"))
                .bio((String) response.get("bio"))
                .image((String) response.get("image"))
                .build());
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for username={}: {}", username, e.getMessage());
    }
    return Optional.empty();
  }
}
