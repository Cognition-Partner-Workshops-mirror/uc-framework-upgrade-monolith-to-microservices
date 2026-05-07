package io.spring.article.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
public class TagsController {

  @GetMapping
  public ResponseEntity<Map<String, List<String>>> getTags() {
    Map<String, List<String>> response = new HashMap<>();
    response.put("tags", List.of());
    return ResponseEntity.ok(response);
  }
}
