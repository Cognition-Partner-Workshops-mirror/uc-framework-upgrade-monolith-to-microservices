package io.spring.articleservice.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.spring.articleservice.core.service.JwtService;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// JWT service for article-service — validates tokens issued by user-service (monolith)
// Uses the same shared secret so tokens are portable across services
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;

  public DefaultJwtService(@Value("${jwt.secret}") String secret) {
    // Use Keys.hmacShaKeyFor for proper key derivation (JJWT 0.12.x)
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Parse and verify the JWT using the shared signing key
      Jws<Claims> claimsJws =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return Optional.ofNullable(claimsJws.getPayload().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
