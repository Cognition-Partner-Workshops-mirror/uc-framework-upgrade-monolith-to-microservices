package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Standalone Spring Boot 3 microservice for the Article bounded context.
// Extracted from the monolith to own articles, tags, comments, and favorites.
@SpringBootApplication
public class ArticleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
