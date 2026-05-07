package io.spring.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Standalone Spring Boot application for the extracted article microservice
@SpringBootApplication
public class ArticleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
