package io.spring.articleservice.client;

import io.spring.articleservice.dto.ProfileDTO;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

// REST client for cross-service communication with user-service.
// Article-service calls user-service to resolve user profiles for article authors.
// Falls back gracefully if user-service is unavailable.
@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  // Fetch user profile from user-service by username.
  // Returns empty if user-service is unreachable or user not found.
  @SuppressWarnings("unchecked")
  public Optional<ProfileDTO> getProfile(String username) {
    try {
      String url = userServiceUrl + "/profiles/" + username;
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("profile")) {
        Map<String, Object> profile = (Map<String, Object>) response.get("profile");
        return Optional.of(
            ProfileDTO.builder()
                .username((String) profile.get("username"))
                .bio((String) profile.get("bio"))
                .image((String) profile.get("image"))
                .following(Boolean.TRUE.equals(profile.get("following")))
                .build());
      }
    } catch (RestClientException e) {
      // Graceful degradation: return empty profile if user-service is unavailable
      logger.warn("Failed to fetch profile for user '{}' from user-service: {}",
          username, e.getMessage());
    }
    return Optional.empty();
  }

  // Validate a JWT token by calling user-service's current user endpoint.
  // Returns the user ID if valid, empty otherwise.
  @SuppressWarnings("unchecked")
  public Optional<String> validateToken(String token) {
    try {
      String url = userServiceUrl + "/user";
      // In a production setup, this would pass the Authorization header
      // For now, token validation is done locally via shared JWT secret
      return Optional.empty();
    } catch (RestClientException e) {
      logger.warn("Failed to validate token with user-service: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
