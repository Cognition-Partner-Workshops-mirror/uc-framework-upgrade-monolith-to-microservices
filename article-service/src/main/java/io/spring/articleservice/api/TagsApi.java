package io.spring.articleservice.api;

import io.spring.articleservice.infrastructure.mybatis.readservice.TagReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Tags API — returns all available tags from the article-service database
@RestController
@RequestMapping("/tags")
@AllArgsConstructor
public class TagsApi {

  private TagReadService tagReadService;

  // List all tags — public endpoint
  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagReadService.allTags());
    return ResponseEntity.ok(response);
  }
}
