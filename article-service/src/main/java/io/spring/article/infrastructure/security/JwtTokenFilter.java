package io.spring.article.infrastructure.security;

import io.spring.article.application.client.UserServiceClient;
import io.spring.article.application.dto.UserDTO;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT token filter for article-service. Parses the JWT to extract the user ID,
 * then calls user-service to fetch user details. Sets a lightweight UserDTO
 * principal in the SecurityContext instead of the full User entity.
 */
public class JwtTokenFilter extends OncePerRequestFilter {
  private final SecretKey signingKey;
  private final UserServiceClient userServiceClient;
  private final String header = "Authorization";

  public JwtTokenFilter(
      @Value("${jwt.secret}") String secret, UserServiceClient userServiceClient) {
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenString(request.getHeader(header))
        .flatMap(this::getUserIdFromToken)
        .ifPresent(
            userId -> {
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Cross-service call to user-service for user lookup
                try {
                  UserDTO user = userServiceClient.getUserById(userId);
                  if (user != null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                            user, null, Collections.emptyList());
                    authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                  }
                } catch (Exception e) {
                  // Graceful degradation: if user-service is unreachable, skip auth
                  logger.warn("Failed to fetch user from user-service", e);
                }
              }
            });

    filterChain.doFilter(request, response);
  }

  // Extract userId from JWT using JJWT 0.12.x API
  private Optional<String> getUserIdFromToken(String token) {
    try {
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

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
