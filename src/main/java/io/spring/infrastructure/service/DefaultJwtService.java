package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Date;
import java.util.Optional;
// javax.crypto is part of Java SE — NOT Jakarta EE — so these imports remain unchanged
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  // Migrated from SignatureAlgorithm enum to MacAlgorithm (JJWT 0.12.x)
  private final MacAlgorithm algorithm;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Use Keys.hmacShaKeyFor to auto-select the right HMAC algorithm based on key length
    byte[] keyBytes = secret.getBytes();
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    // Determine algorithm from key size: 256-bit→HS256, 384-bit→HS384, 512-bit→HS512
    if (keyBytes.length >= 64) {
      this.algorithm = Jwts.SIG.HS512;
    } else if (keyBytes.length >= 48) {
      this.algorithm = Jwts.SIG.HS384;
    } else {
      this.algorithm = Jwts.SIG.HS256;
    }
  }

  @Override
  public String toToken(User user) {
    // Migrated from setSubject/setExpiration/signWith to JJWT 0.12.x builder API
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        .signWith(signingKey, algorithm)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Migrated from parserBuilder().setSigningKey() to Jwts.parser().verifyWith() (JJWT 0.12.x)
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
