package io.spring.articleservice.client;

import io.spring.articleservice.dto.ProfileDto;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for communicating with the user-service (monolith). Resolves user profile data by
 * userId for embedding in article/comment responses. Falls back to a default profile if the
 * user-service is unreachable.
 */
@Component
public class UserServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

  private final RestTemplate restTemplate;
  private final String userServiceBaseUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.base-url:http://localhost:8080}") String userServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.userServiceBaseUrl = userServiceBaseUrl;
  }

  /**
   * Fetch user profile from the user-service by userId. Returns Optional.empty() if the
   * user-service is unreachable or user not found.
   */
  public Optional<ProfileDto> getProfileByUserId(String userId) {
    try {
      String url = userServiceBaseUrl + "/api/users/" + userId + "/profile";
      ProfileDto profile = restTemplate.getForObject(url, ProfileDto.class);
      return Optional.ofNullable(profile);
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for userId={}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Fetch user profile from the user-service by username. Returns Optional.empty() if the
   * user-service is unreachable or user not found.
   */
  public Optional<ProfileDto> getProfileByUsername(String username) {
    try {
      String url = userServiceBaseUrl + "/api/profiles/" + username;
      ProfileDto profile = restTemplate.getForObject(url, ProfileDto.class);
      return Optional.ofNullable(profile);
    } catch (Exception e) {
      logger.warn("Failed to fetch profile for username={}: {}", username, e.getMessage());
      return Optional.empty();
    }
  }
}
