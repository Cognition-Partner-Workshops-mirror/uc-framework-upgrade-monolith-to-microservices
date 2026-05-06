package io.spring.article.api;

import io.spring.article.infrastructure.mybatis.readservice.TagReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@AllArgsConstructor
public class TagsApi {

  private TagReadService tagReadService;

  @GetMapping
  public ResponseEntity<Map<String, Object>> getTags() {
    Map<String, Object> response = new HashMap<>();
    response.put("tags", tagReadService.allTags());
    return ResponseEntity.ok(response);
  }
}
