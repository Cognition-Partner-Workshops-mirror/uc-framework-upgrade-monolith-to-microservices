package io.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Spring Boot 3 / Spring Security 6 requires web environment for SecurityFilterChain bean resolution
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RealworldApplicationTests {

  @Test
  public void contextLoads() {}
}
