package io.spring.article.infrastructure.client;

import io.spring.article.application.ProfileData;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// REST client for cross-service communication with user-service.
// Fetches user profiles and validates user existence for article operations.
@Component
public class UserServiceClient {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate, @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  // Fetch a user profile by username from the user-service.
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfile(String username) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              userServiceUrl + "/profiles/{username}", Map.class, username);
      if (response != null && response.containsKey("profile")) {
        Map<String, Object> profile = (Map<String, Object>) response.get("profile");
        return Optional.of(
            new ProfileData(
                (String) profile.get("id"),
                (String) profile.get("username"),
                (String) profile.get("bio"),
                (String) profile.get("image"),
                profile.get("following") != null && (Boolean) profile.get("following")));
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for user {}: {}", username, e.getMessage());
    }
    return Optional.empty();
  }

  // Fetch a user profile by user ID from the user-service.
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              userServiceUrl + "/users/{userId}/profile", Map.class, userId);
      if (response != null && response.containsKey("profile")) {
        Map<String, Object> profile = (Map<String, Object>) response.get("profile");
        return Optional.of(
            new ProfileData(
                (String) profile.get("id"),
                (String) profile.get("username"),
                (String) profile.get("bio"),
                (String) profile.get("image"),
                false));
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for userId {}: {}", userId, e.getMessage());
    }
    return Optional.empty();
  }

  // Validate that a user ID exists by calling user-service health-style check.
  public boolean userExists(String userId) {
    return getProfileByUserId(userId).isPresent();
  }
}
