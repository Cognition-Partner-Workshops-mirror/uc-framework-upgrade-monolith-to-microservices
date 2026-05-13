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

// Updated to jjwt 0.12.x API: replaced deprecated SignatureAlgorithm enum with
// MacAlgorithm, and migrated from setSubject/setExpiration/signWith builder methods
// to the new Jwts.builder().subject().expiration().signWith() fluent API.
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Using HmacSHA256 — jjwt 0.12.x enforces strict key size validation;
    // HS512 requires 64+ byte keys, but existing secrets may be shorter.
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
  }

  @Override
  public String toToken(User user) {
    // Migrated from setSubject/setExpiration/signWith to new jjwt 0.12.x fluent API
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        // Explicitly specify HS256 algorithm to avoid jjwt 0.12.x key-size auto-detection issues
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Migrated from parserBuilder().setSigningKey().build().parseClaimsJws()
      // to verifyWith().build().parseSignedClaims() (jjwt 0.12.x API)
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
