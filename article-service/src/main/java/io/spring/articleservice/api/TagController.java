package io.spring.articleservice.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Tag operations in the extracted article-service. Provides endpoint for
 * listing all available tags.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

  /** Get all tags. Returns a list of tag names. */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    // Tags are read from the database — placeholder returns empty list
    // In a full implementation, this would use a TagReadService
    Map<String, Object> response = new HashMap<>();
    response.put("tags", List.of());
    return ResponseEntity.ok(response);
  }
}
