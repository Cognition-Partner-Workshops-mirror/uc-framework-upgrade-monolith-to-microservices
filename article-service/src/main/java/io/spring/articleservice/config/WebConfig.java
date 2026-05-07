package io.spring.articleservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the article-service. Provides RestTemplate bean for cross-service
 * communication with user-service and configures CORS for frontend access.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /** RestTemplate bean used by UserServiceClient for REST calls to user-service. */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /** CORS configuration — allows all origins for development. */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
