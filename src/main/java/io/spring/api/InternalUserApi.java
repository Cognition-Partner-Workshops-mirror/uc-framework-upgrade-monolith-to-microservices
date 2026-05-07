package io.spring.api;

import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API endpoints for cross-service communication.
 * Called by article-service to fetch user data and follow relationships.
 * These endpoints are permitted without authentication (behind service mesh).
 */
@RestController
@RequestMapping("/api/internal")
@AllArgsConstructor
public class InternalUserApi {
  private UserRepository userRepository;
  private UserRelationshipQueryService userRelationshipQueryService;

  // Returns user data (id, username, bio, image) for the given user ID
  @GetMapping("/users/{id}")
  public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") String id) {
    return userRepository
        .findById(id)
        .map(
            user -> {
              Map<String, Object> result = new HashMap<>();
              result.put("id", user.getId());
              result.put("username", user.getUsername());
              result.put("bio", user.getBio());
              result.put("image", user.getImage());
              return ResponseEntity.ok(result);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Returns the set of target IDs that the given user is following
  @GetMapping("/users/{id}/following")
  public ResponseEntity<Set<String>> getFollowingUsers(
      @PathVariable("id") String userId,
      @RequestParam(value = "targetIds", required = false) List<String> targetIds) {
    if (targetIds != null && !targetIds.isEmpty()) {
      Set<String> followingAuthors =
          userRelationshipQueryService.followingAuthors(userId, targetIds);
      return ResponseEntity.ok(followingAuthors);
    } else {
      List<String> followedUsers = userRelationshipQueryService.followedUsers(userId);
      return ResponseEntity.ok(Set.copyOf(followedUsers));
    }
  }

  // Returns whether the given user is following the target user
  @GetMapping("/users/{id}/is-following/{targetId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("id") String userId, @PathVariable("targetId") String targetId) {
    boolean following = userRelationshipQueryService.isUserFollowing(userId, targetId);
    return ResponseEntity.ok(following);
  }
}
