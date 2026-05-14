package io.spring.articleservice.core.service;

import java.util.Optional;

// JWT service interface for token validation in the article microservice
public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
