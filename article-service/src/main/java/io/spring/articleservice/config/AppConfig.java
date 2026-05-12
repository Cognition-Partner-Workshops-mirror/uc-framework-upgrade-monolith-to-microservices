package io.spring.articleservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Application configuration beans for the Article microservice
@Configuration
public class AppConfig {

  // RestTemplate bean used by UserServiceClient for cross-service REST calls
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
