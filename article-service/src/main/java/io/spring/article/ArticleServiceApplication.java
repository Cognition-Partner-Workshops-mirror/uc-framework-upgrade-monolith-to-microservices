package io.spring.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Standalone microservice extracted from the monolith for the Article bounded context.
// Handles articles, tags, comments, and favorites independently.
@SpringBootApplication
public class ArticleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
