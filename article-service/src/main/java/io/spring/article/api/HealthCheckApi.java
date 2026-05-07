package io.spring.article.api;

import io.spring.article.application.client.UserServiceClient;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Custom health endpoint that also checks connectivity to user-service
@RestController
@AllArgsConstructor
public class HealthCheckApi {

  private UserServiceClient userServiceClient;

  @GetMapping("/api/health")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("service", "article-service");

    // Check user-service connectivity
    try {
      userServiceClient.getUserById("health-check");
      health.put("userService", "UP");
    } catch (Exception e) {
      health.put("userService", "DOWN");
      health.put("userServiceError", e.getMessage());
    }

    return ResponseEntity.ok(health);
  }
}
