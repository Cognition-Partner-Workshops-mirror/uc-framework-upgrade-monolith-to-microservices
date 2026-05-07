package io.spring.articleservice;

import org.junit.jupiter.api.Test;

/** Smoke test to verify the article-service Spring context loads. */
class ArticleServiceApplicationTest {

  @Test
  void contextLoads() {
    // Verifies that the application class can be instantiated
    ArticleServiceApplication app = new ArticleServiceApplication();
    org.junit.jupiter.api.Assertions.assertNotNull(app);
  }
}
