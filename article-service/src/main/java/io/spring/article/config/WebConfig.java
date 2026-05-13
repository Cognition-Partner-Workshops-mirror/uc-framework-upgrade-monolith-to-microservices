package io.spring.article.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Configuration for cross-service HTTP communication.
@Configuration
public class WebConfig {

  // RestTemplate bean used by UserServiceClient for REST calls to user-service.
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
