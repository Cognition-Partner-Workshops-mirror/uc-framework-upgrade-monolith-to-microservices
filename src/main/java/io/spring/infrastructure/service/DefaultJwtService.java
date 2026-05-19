package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Updated for jjwt 0.12.x API changes:
// - Removed SignatureAlgorithm enum (deprecated) in favor of Jwts.SIG constants
// - Replaced .setSubject()/.setExpiration() with .subject()/.expiration()
// - Replaced Jwts.parserBuilder() with Jwts.parser()
// - Replaced .parseClaimsJws() with .parseSignedClaims()
// - Replaced .getBody() with .getPayload()
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Use HmacSHA512 algorithm name directly instead of deprecated SignatureAlgorithm enum
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  @Override
  public String toToken(User user) {
    // Migrated from deprecated builder methods to jjwt 0.12.x fluent API
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        .signWith(signingKey)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Migrated from parserBuilder().setSigningKey().build().parseClaimsJws()
      // to parser().verifyWith().build().parseSignedClaims() (jjwt 0.12.x)
      Jws<Claims> claimsJws =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      return Optional.ofNullable(claimsJws.getPayload().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Date expireTimeFromNow() {
    return new Date(System.currentTimeMillis() + sessionTime * 1000L);
  }
}
