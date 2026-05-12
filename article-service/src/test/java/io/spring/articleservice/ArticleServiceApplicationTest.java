package io.spring.articleservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Smoke test - verifies that the article-service Spring context loads successfully
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ArticleServiceApplicationTest {

  @Test
  void contextLoads() {}
}
