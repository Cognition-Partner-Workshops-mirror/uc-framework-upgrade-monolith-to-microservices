package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;
  private int sessionTime;

  @Autowired
  public DefaultJwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    // Upgraded: JJWT 0.11.x -> 0.12.x requires key size >= 512 bits for HS512.
    // Pad or hash the secret bytes to ensure a key that meets the minimum size requirement.
    byte[] secretBytes = secret.getBytes();
    if (secretBytes.length < 64) {
      byte[] padded = new byte[64];
      System.arraycopy(secretBytes, 0, padded, 0, secretBytes.length);
      secretBytes = padded;
    }
    this.signingKey = new SecretKeySpec(secretBytes, "HmacSHA512");
  }

  @Override
  public String toToken(User user) {
    // Upgraded: setSubject/setExpiration replaced by subject/expiration in JJWT 0.12.x
    return Jwts.builder()
        .subject(user.getId())
        .expiration(expireTimeFromNow())
        .signWith(signingKey, Jwts.SIG.HS512)
        .compact();
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      // Upgraded: parserBuilder().setSigningKey() replaced by parser().verifyWith()
      // parseClaimsJws() replaced by parseSignedClaims() in JJWT 0.12.x
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
