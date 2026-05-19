package io.spring.articleservice.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Custom health check endpoint for the article-service.
// Supplements Spring Boot Actuator's /actuator/health with service-specific info.
@RestController
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("service", "article-service");
    health.put("version", "1.0.0");
    return ResponseEntity.ok(health);
  }
}
