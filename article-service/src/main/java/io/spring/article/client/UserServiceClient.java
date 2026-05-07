package io.spring.article.client;

import io.spring.article.dto.AuthorDto;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

  private final RestTemplate restTemplate;
  private final String userServiceUrl;

  public UserServiceClient(
      RestTemplate restTemplate,
      @Value("${user-service.url:http://localhost:8080}") String userServiceUrl) {
    this.restTemplate = restTemplate;
    this.userServiceUrl = userServiceUrl;
  }

  public Optional<AuthorDto> getUserById(String userId) {
    try {
      AuthorDto author =
          restTemplate.getForObject(userServiceUrl + "/api/users/{userId}", AuthorDto.class, userId);
      return Optional.ofNullable(author);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<AuthorDto> getUserByUsername(String username) {
    try {
      AuthorDto author =
          restTemplate.getForObject(
              userServiceUrl + "/api/users/username/{username}", AuthorDto.class, username);
      return Optional.ofNullable(author);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
