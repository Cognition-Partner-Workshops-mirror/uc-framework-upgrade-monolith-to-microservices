package io.spring.articleservice.api;

import io.spring.articleservice.infrastructure.mybatis.readservice.TagReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Tags API - returns all available tags
// Extracted from monolith TagsApi
@RestController
@RequestMapping(path = "/tags")
@AllArgsConstructor
public class TagsApi {
  private TagReadService tagReadService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    return ResponseEntity.ok(
        new HashMap<>() {
          {
            put("tags", tagReadService.allTags());
          }
        });
  }
}
