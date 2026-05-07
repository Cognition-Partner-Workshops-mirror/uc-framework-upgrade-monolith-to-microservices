package io.spring.articleservice.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Custom health check endpoint for the article-service. Supplements the Spring Boot Actuator
 * /actuator/health endpoint with a simple service-specific health response.
 */
@RestController
public class HealthController {

  /** Returns service name and status for load balancer / Docker health checks. */
  @GetMapping("/api/health")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(
        Map.of(
            "service", "article-service",
            "status", "UP"));
  }
}
