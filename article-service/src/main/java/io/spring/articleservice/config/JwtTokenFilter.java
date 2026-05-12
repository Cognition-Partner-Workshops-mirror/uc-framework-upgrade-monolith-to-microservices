package io.spring.articleservice.config;

import io.spring.articleservice.infrastructure.service.JwtService;
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

// JWT authentication filter - validates tokens using shared secret with user-service
// Extracts userId from JWT and sets it as the authentication principal
public class JwtTokenFilter extends OncePerRequestFilter {

  @Autowired private JwtService jwtService;

  // Extract JWT from Authorization header and set security context
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    Optional<String> tokenOptional = getTokenFromHeader(request);
    if (tokenOptional.isPresent()) {
      Optional<String> subOptional = jwtService.getSubFromToken(tokenOptional.get());
      if (subOptional.isPresent()) {
        // Set userId as the principal - article-service doesn't need full User object
        String userId = subOptional.get();
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenFromHeader(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Token ")) {
      return Optional.empty();
    }
    return Optional.of(authorization.substring(6));
  }
}
