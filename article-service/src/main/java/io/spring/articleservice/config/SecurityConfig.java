package io.spring.articleservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the article-service. Uses the Spring Boot 3 SecurityFilterChain bean
 * pattern with stateless session management. JWT validation is delegated to the API gateway /
 * user-service in production.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Configures HTTP security with permissive rules for the article-service. In production, JWT
   * validation would be handled by a gateway or filter.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Health check endpoint is public
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    // Public read endpoints
                    .requestMatchers(HttpMethod.GET, "/api/articles/**", "/api/tags")
                    .permitAll()
                    // Allow all for now — in production, JWT filter would gate writes
                    .anyRequest()
                    .permitAll());
    return http.build();
  }
}
