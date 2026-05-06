package io.spring.api;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
@AllArgsConstructor
public class InternalProfileApi {

  private ProfileQueryService profileQueryService;
  private UserRepository userRepository;

  @GetMapping("/profiles/{userId}")
  public ResponseEntity<ProfileData> getProfileById(@PathVariable("userId") String userId) {
    return userRepository
        .findById(userId)
        .flatMap(user -> profileQueryService.findByUsername(user.getUsername(), null))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/profiles/by-username/{username}")
  public ResponseEntity<ProfileData> getProfileByUsername(
      @PathVariable("username") String username) {
    return profileQueryService
        .findByUsername(username, null)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/users/{userId}/following/{targetUserId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable("userId") String userId, @PathVariable("targetUserId") String targetUserId) {
    Optional<io.spring.core.user.FollowRelation> relation =
        userRepository.findRelation(userId, targetUserId);
    return ResponseEntity.ok(relation.isPresent());
  }
}
