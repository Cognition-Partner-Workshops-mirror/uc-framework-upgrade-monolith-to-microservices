package io.spring.articleservice.infrastructure.client;

import io.spring.articleservice.application.data.ProfileData;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

// REST client for cross-service communication with user-service
// Retrieves user profile data needed for article/comment author information
@Component
public class UserServiceClient {

  private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  // Fetch user profile by username from user-service /profiles/{username} endpoint
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfile(String username) {
    try {
      String url = userServiceUrl + "/profiles/" + username;
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("profile")) {
        Map<String, Object> profile = (Map<String, Object>) response.get("profile");
        ProfileData profileData =
            new ProfileData(
                (String) profile.getOrDefault("id", ""),
                (String) profile.get("username"),
                (String) profile.get("bio"),
                (String) profile.get("image"),
                Boolean.TRUE.equals(profile.get("following")));
        return Optional.of(profileData);
      }
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for user '{}' from user-service: {}", username, e.getMessage());
      return Optional.empty();
    }
  }

  // Fetch user profile by userId from user-service internal endpoint
  @SuppressWarnings("unchecked")
  public Optional<ProfileData> getProfileByUserId(String userId) {
    try {
      String url = userServiceUrl + "/internal/users/" + userId + "/profile";
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null) {
        ProfileData profileData =
            new ProfileData(
                (String) response.getOrDefault("id", userId),
                (String) response.get("username"),
                (String) response.get("bio"),
                (String) response.get("image"),
                false);
        return Optional.of(profileData);
      }
      return Optional.empty();
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for userId '{}' from user-service: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }
}
