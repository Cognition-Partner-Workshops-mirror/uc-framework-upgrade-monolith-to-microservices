package io.spring.article.api;

import io.spring.article.infrastructure.mybatis.mapper.TagReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Tags REST controller — returns all available tags.
// Part of the Article bounded context since tags are article metadata.
@RestController
@RequestMapping("/tags")
@AllArgsConstructor
public class TagsApi {

  private TagReadService tagReadService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagReadService.all());
    return ResponseEntity.ok(response);
  }
}
