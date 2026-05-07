# Spring Boot 2 → 3 Upgrade Notes

## Overview
This document captures every breaking change encountered during the upgrade from Spring Boot 2.6.3 to Spring Boot 3.2.3 and how each was resolved.

---

## 1. Build System Changes

### Gradle Wrapper: 7.4 → 8.6
- **File:** `gradle/wrapper/gradle-wrapper.properties`
- **Change:** Updated `distributionUrl` from `gradle-7.4-bin.zip` to `gradle-8.6-bin.zip`
- **Reason:** Spring Boot 3.x requires Gradle 7.5+ (8.x recommended)

### Spring Boot Plugin: 2.6.3 → 3.2.3
- **File:** `build.gradle`
- **Change:** Updated `org.springframework.boot` plugin version
- **Reason:** Core framework upgrade

### Dependency Management Plugin: 1.0.11.RELEASE → 1.1.4
- **File:** `build.gradle`
- **Change:** Updated `io.spring.dependency-management` plugin version
- **Reason:** Compatibility with Spring Boot 3.x BOM

### Java Version: 11 → 17
- **Files:** `build.gradle`, `.java-version`
- **Change:** Updated `sourceCompatibility` and `targetCompatibility` from `11` to `17`
- **Reason:** Spring Boot 3.x requires Java 17 as minimum baseline

---

## 2. Jakarta EE Migration (javax → jakarta)

### Breaking Change
Spring Boot 3.x moved from Java EE (`javax.*`) to Jakarta EE (`jakarta.*`) namespaces. This is the single largest breaking change in the upgrade.

### Files Modified (25+ files)

#### Servlet API
- **`JwtTokenFilter.java`**: `javax.servlet.*` → `jakarta.servlet.*`
  - `FilterChain`, `ServletException`, `HttpServletRequest`, `HttpServletResponse`

#### Validation API
- **`ArticleApi.java`**: `javax.validation.Valid` → `jakarta.validation.Valid`
- **`ArticlesApi.java`**: `javax.validation.Valid` → `jakarta.validation.Valid`
- **`CommentsApi.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`CurrentUserApi.java`**: `javax.validation.Valid` → `jakarta.validation.Valid`
- **`UsersApi.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`CustomizeExceptionHandler.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`ArticleCommandService.java`**: `javax.validation.Valid` → `jakarta.validation.Valid`
- **`NewArticleParam.java`**: `javax.validation.constraints.*` → `jakarta.validation.constraints.*`
- **`DuplicatedArticleConstraint.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`DuplicatedArticleValidator.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`DuplicatedEmailConstraint.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`DuplicatedEmailValidator.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`DuplicatedUsernameConstraint.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`DuplicatedUsernameValidator.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`RegisterParam.java`**: `javax.validation.constraints.*` → `jakarta.validation.constraints.*`
- **`UpdateUserParam.java`**: `javax.validation.constraints.*` → `jakarta.validation.constraints.*`
- **`UserService.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`UserMutation.java`**: `javax.validation.*` → `jakarta.validation.*`
- **`GraphQLCustomizeExceptionHandler.java`**: `javax.validation.*` → `jakarta.validation.*`

---

## 3. Spring Security Changes

### WebSecurityConfigurerAdapter Removed
- **File:** `WebSecurityConfig.java`
- **Breaking Change:** `WebSecurityConfigurerAdapter` was deprecated in Spring Security 5.7 and removed in 6.0 (shipped with Spring Boot 3.x)
- **Resolution:** Converted to `SecurityFilterChain` bean-based configuration

#### Before (Spring Boot 2):
```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/articles/**").permitAll()
            ...
    }
}
```

#### After (Spring Boot 3):
```java
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.GET, "/articles/**").permitAll()
                ...
            );
        return http.build();
    }
}
```

### Key API Changes:
- `.csrf().disable()` → `.csrf(AbstractHttpConfigurer::disable)`
- `.cors().and()` → `.cors(cors -> cors.configurationSource(...))`
- `.authorizeRequests()` → `.authorizeHttpRequests()`
- `.antMatchers()` → `.requestMatchers()`
- Lambda-based DSL for all configuration blocks

---

## 4. JJWT Library: 0.11.2 → 0.12.5

- **File:** `DefaultJwtService.java`
- **Breaking Changes:**
  - `SignatureAlgorithm` enum removed; key determines algorithm
  - `Jwts.builder().setSubject()` → `Jwts.builder().subject()`
  - `Jwts.builder().setExpiration()` → `Jwts.builder().expiration()`
  - `Jwts.parserBuilder().setSigningKey().build().parseClaimsJws()` → `Jwts.parser().verifyWith().build().parseSignedClaims()`
  - `claimsJws.getBody()` → `claimsJws.getPayload()`
  - Key construction: `new SecretKeySpec(secret.getBytes(), algo)` → `Keys.hmacShaKeyFor(secret.getBytes())`

---

## 5. Netflix DGS Framework: 4.9.21 → 8.7.1

- **File:** `build.gradle`
- **Change:** Updated from `graphql-dgs-spring-boot-starter:4.9.21` to `graphql-dgs-spring-boot-starter:8.7.1`

### DGS Codegen: 5.0.6 → 6.0.3
- **File:** `build.gradle`
- **Change:** Updated codegen plugin for compatibility with DGS 8.x

### PageInfo Type Conflict
- **Files:** `ArticleDatafetcher.java`, `CommentDatafetcher.java`
- **Breaking Change:** `graphql.relay.DefaultPageInfo` (graphql-java) is not assignable to DGS-generated `io.spring.graphql.types.PageInfo`
- **Resolution:** Replaced `graphql.relay.DefaultPageInfo` construction with `io.spring.graphql.types.PageInfo.newBuilder()` pattern

### DataFetcherExceptionHandler API Change
- **File:** `GraphQLCustomizeExceptionHandler.java`
- **Breaking Change:** `DataFetcherExceptionHandler.onException()` changed to `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`
- **Resolution:** Updated method signature and wrapped results in `CompletableFuture.completedFuture()`

---

## 6. ResponseEntityExceptionHandler Signature Change

- **File:** `CustomizeExceptionHandler.java`
- **Breaking Change:** `handleMethodArgumentNotValid()` parameter type changed from `HttpStatus` to `HttpStatusCode`
- **Resolution:** Updated method signature to use `HttpStatusCode` and added the import

---

## 7. Other Dependency Upgrades

| Dependency | Old Version | New Version | Reason |
|---|---|---|---|
| MyBatis Spring Boot | 2.2.2 | 3.0.3 | Spring Boot 3 compatibility |
| Joda-Time | 2.10.13 | 2.12.7 | Latest stable |
| SQLite JDBC | 3.36.0.3 | 3.45.1.0 | Latest stable |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 support |
| Spotless | 6.2.1 | 6.25.0 | Gradle 8.x compatibility |
| REST Assured | 4.5.1 | 5.4.0 | Jakarta EE support |
| Mockito Inline | 4.0.0 | 5.2.0 | Java 17 support |
| Spring Boot Actuator | N/A | Added | Health check endpoints |

---

## 8. Selenium/WebDriver (Test Code)

- **File:** `BasePage.java` (test)
- **Breaking Change:** `WebDriverWait` constructor no longer accepts `long` for timeout; requires `java.time.Duration`
- **Resolution:** Changed `new WebDriverWait(driver, seconds)` to `new WebDriverWait(driver, Duration.ofSeconds(seconds))`
