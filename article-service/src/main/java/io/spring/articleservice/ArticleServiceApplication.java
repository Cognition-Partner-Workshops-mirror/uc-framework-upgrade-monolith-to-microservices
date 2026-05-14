package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Standalone Spring Boot 3 microservice extracted from the RealWorld monolith
// Handles the Article bounded context: articles, tags, favorites, and comments
@SpringBootApplication
public class ArticleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
