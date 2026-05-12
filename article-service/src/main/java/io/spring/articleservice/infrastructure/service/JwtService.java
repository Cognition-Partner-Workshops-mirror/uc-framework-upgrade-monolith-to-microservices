package io.spring.articleservice.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// JWT service for token validation - uses shared secret with user-service
// Only needs to parse/validate tokens, not create them (user-service creates tokens)
// Uses JJWT 0.12.x API: parser().verifyWith().build().parseSignedClaims()
@Component
public class JwtService {
  private final SecretKey signingKey;

  public JwtService(@Value("${jwt.secret}") String secret) {
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  // Extract userId (subject) from a JWT token
  public Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return Optional.ofNullable(claimsJws.getPayload().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
