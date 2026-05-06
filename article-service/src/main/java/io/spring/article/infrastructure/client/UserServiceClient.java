package io.spring.article.infrastructure.client;

import io.spring.article.application.data.ProfileData;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

  public Optional<ProfileData> getProfileById(String userId) {
    try {
      ProfileData profile =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/profiles/{userId}", ProfileData.class, userId);
      return Optional.ofNullable(profile);
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for userId={}: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<ProfileData> getProfileByUsername(String username) {
    try {
      ProfileData profile =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/profiles/by-username/{username}",
              ProfileData.class,
              username);
      return Optional.ofNullable(profile);
    } catch (RestClientException e) {
      log.warn("Failed to fetch profile for username={}: {}", username, e.getMessage());
      return Optional.empty();
    }
  }

  public boolean isFollowing(String userId, String targetUserId) {
    try {
      Boolean result =
          restTemplate.getForObject(
              userServiceUrl + "/api/internal/users/{userId}/following/{targetUserId}",
              Boolean.class,
              userId,
              targetUserId);
      return Boolean.TRUE.equals(result);
    } catch (RestClientException e) {
      log.warn("Failed to check following status: {}", e.getMessage());
      return false;
    }
  }
}
