package io.spring.api;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Internal API for cross-service communication — used by article-service to resolve user profiles
// These endpoints are not exposed to external clients and should be secured in production
@RestController
@RequestMapping("/api/internal/users")
public class InternalUserApi {

  private final UserRepository userRepository;

  public InternalUserApi(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // Lookup user by ID — used by article-service to display article/comment author info
  @GetMapping("/{userId}")
  public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
    return userRepository
        .findById(userId)
        .map(this::toUserMap)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Lookup user by username — used by article-service for profile resolution
  @GetMapping("/by-username/{username}")
  public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
    return userRepository
        .findByUsername(username)
        .map(this::toUserMap)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Convert User entity to a simple map DTO for cross-service transfer
  private Map<String, Object> toUserMap(User user) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", user.getId());
    map.put("username", user.getUsername());
    map.put("bio", user.getBio());
    map.put("image", user.getImage());
    return map;
  }
}
