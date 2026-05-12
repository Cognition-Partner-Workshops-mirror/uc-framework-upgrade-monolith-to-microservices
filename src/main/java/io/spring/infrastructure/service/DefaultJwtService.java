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

// Migrated from JJWT 0.11.x to 0.12.x API:
// - Removed deprecated SignatureAlgorithm enum (algorithm now inferred from key)
// - Replaced setSubject() with subject() (new builder API)
// - Replaced setExpiration() with expiration() (new builder API)
// - Replaced parserBuilder().setSigningKey().build().parseClaimsJws()
//   with parser().verifyWith().build().parseSignedClaims() (new parser API)
// - Replaced getBody() with getPayload() for accessing JWT claims
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Using HmacSHA512 algorithm — key spec unchanged, but SignatureAlgorithm enum removed in 0.12
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  @Override
  public String toToken(User user) {
    // Migrated from setSubject/setExpiration to subject/expiration (JJWT 0.12 builder API)
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        .signWith(signingKey)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Migrated from parserBuilder/parseClaimsJws to parser/parseSignedClaims (JJWT 0.12 API)
      Jws<Claims> claimsJws =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
      // Migrated from getBody() to getPayload() (JJWT 0.12 API)
      return Optional.ofNullable(claimsJws.getPayload().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Date expireTimeFromNow() {
    return new Date(System.currentTimeMillis() + sessionTime * 1000L);
  }
}
