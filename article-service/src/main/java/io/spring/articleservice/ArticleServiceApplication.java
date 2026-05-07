package io.spring.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standalone Article microservice extracted from the RealWorld monolith. Owns the Article bounded
 * context: articles, tags, comments, favorites. Communicates with the user-service via REST for
 * user profile resolution.
 */
@SpringBootApplication
public class ArticleServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArticleServiceApplication.class, args);
  }
}
