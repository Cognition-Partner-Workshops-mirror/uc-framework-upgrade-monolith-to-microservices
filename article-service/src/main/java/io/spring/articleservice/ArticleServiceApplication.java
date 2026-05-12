package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Main entry point for the Article microservice
// Extracted from the monolith's Article bounded context
@SpringBootApplication
public class ArticleServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
