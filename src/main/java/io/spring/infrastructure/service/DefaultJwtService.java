package io.spring.infrastructure.service;

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

// JJWT 0.12.x API: builder methods renamed (setSubject->subject, setExpiration->expiration),
// parser changed (parserBuilder->parser, parseClaimsJws->parseSignedClaims, getBody->getPayload)
@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Use HmacSHA512 directly instead of deprecated SignatureAlgorithm enum
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  @Override
  public String toToken(User user) {
    // JJWT 0.12: setSubject() -> subject(), setExpiration() -> expiration()
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        .signWith(signingKey, Jwts.SIG.HS512)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // JJWT 0.12: parserBuilder() -> parser(), setSigningKey() -> verifyWith(),
      // parseClaimsJws() -> parseSignedClaims(), getBody() -> getPayload()
      return Optional.ofNullable(
          Jwts.parser()
              .verifyWith(signingKey)
              .build()
              .parseSignedClaims(token)
              .getPayload()
              .getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Date expireTimeFromNow() {
    return new Date(System.currentTimeMillis() + sessionTime * 1000L);
  }
}
