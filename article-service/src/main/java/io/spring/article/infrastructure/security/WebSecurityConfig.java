package io.spring.article.infrastructure.security;

import static java.util.Arrays.asList;

import io.spring.article.application.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Spring Security 6 configuration using SecurityFilterChain bean pattern with lambda DSL
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public JwtTokenFilter jwtTokenFilter(
      @Value("${jwt.secret}") String jwtSecret, UserServiceClient userServiceClient) {
    return new JwtTokenFilter(jwtSecret, userServiceClient);
  }

  // SecurityFilterChain for article-service: permits article read endpoints, requires auth for writes
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtTokenFilter jwtTokenFilter) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .exceptionHandling(
            ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // Actuator health endpoints
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    // Custom health endpoint
                    .requestMatchers("/api/health")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/articles/feed")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/articles/**", "/tags")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(asList("*"));
    configuration.setAllowedMethods(asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
    configuration.setAllowCredentials(false);
    configuration.setAllowedHeaders(asList("Authorization", "Cache-Control", "Content-Type"));
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
