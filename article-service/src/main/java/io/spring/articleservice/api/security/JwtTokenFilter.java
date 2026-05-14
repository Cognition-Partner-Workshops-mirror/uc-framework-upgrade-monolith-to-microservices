package io.spring.articleservice.api.security;

import io.spring.articleservice.core.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT filter for article-service — extracts user ID from the JWT token
// Sets the authenticated user ID as the principal for downstream controllers
public class JwtTokenFilter extends OncePerRequestFilter {
  @Autowired private JwtService jwtService;

  // Header format: "Token <jwt>" (RealWorld API convention)
  private String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenString(request.getHeader(header))
        .ifPresent(
            token -> {
              // Extract user ID from the JWT subject claim
              jwtService
                  .getSubFromToken(token)
                  .ifPresent(
                      userId -> {
                        // Store user ID as the principal for cross-service user resolution
                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userId, null, Collections.emptyList());
                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                      });
            });
    filterChain.doFilter(request, response);
  }

  // Parse "Token <jwt>" format from the Authorization header
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
