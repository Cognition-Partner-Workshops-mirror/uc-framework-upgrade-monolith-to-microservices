# Upgrade Notes: Spring Boot 2.6.3 → 3.2.4

This document records every breaking change encountered during the framework upgrade and how it was resolved.

## 1. Build System Changes

### Gradle Wrapper: 7.4 → 8.7
- **Why**: Spring Boot 3.2.x requires Gradle 7.5+ and recommends 8.x for full compatibility.
- **Change**: Updated `gradle/wrapper/gradle-wrapper.properties` to use `gradle-8.7-bin.zip`.

### Java Version: 11 → 17
- **Why**: Spring Boot 3.x requires Java 17 as minimum (uses Jakarta EE 10 APIs compiled for Java 17).
- **Change**: Updated `sourceCompatibility` and `targetCompatibility` to `'17'` in `build.gradle`. Updated `.java-version` file.

## 2. Dependency Upgrades

| Dependency | Old Version | New Version | Reason |
|---|---|---|---|
| Spring Boot | 2.6.3 | 3.2.4 | Jakarta EE 10, Spring Framework 6.x |
| Spring Dependency Management | 1.0.11.RELEASE | 1.1.4 | Compatible with Spring Boot 3.x |
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 | Spring Boot 3 / Jakarta compatibility |
| DGS Codegen Plugin | 5.0.6 | 6.1.0 | Spring Boot 3 compatible codegen |
| DGS Spring Boot Starter | 4.9.21 | 8.4.1 | Major API changes for Spring Boot 3 |
| JJWT (API + impl + jackson) | 0.11.2 | 0.12.5 | Deprecated API removal, stricter key validation |
| SQLite JDBC | 3.36.0.3 | 3.45.1.0 | Bug fixes, performance improvements |
| Spotless Plugin | 6.2.1 | 6.25.0 | Java 17 module system compatibility |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 bytecode support |
| rest-assured | 4.5.1 | 5.4.0 | Spring Boot 3 / Jakarta Servlet compatibility |
| Mockito | 4.x (managed) | 5.11.0 | Java 17 compatibility |
| MyBatis Test Starter | 2.2.2 | 3.0.3 | Spring Boot 3 compatibility |

### New Dependencies Added
- `spring-boot-starter-actuator` — health check and monitoring endpoints for microservice orchestration.

## 3. Jakarta EE Migration (javax → jakarta)

### Breaking Change
Spring Boot 3 / Jakarta EE 10 renamed the `javax.servlet` and `javax.validation` packages to `jakarta.servlet` and `jakarta.validation`.

### Files Changed
All `.java` files containing `javax.servlet.*` or `javax.validation.*` imports were updated:
- `JwtTokenFilter.java` — `javax.servlet.FilterChain` → `jakarta.servlet.FilterChain`, etc.
- `CustomizeExceptionHandler.java` — `javax.validation.ConstraintViolation` → `jakarta.validation.ConstraintViolation`
- `GraphQLCustomizeExceptionHandler.java` — same validation import changes
- All controller classes (`ArticlesApi`, `ArticleApi`, `CommentsApi`, `UsersApi`, `CurrentUserApi`)
- All validation constraint classes (`DuplicatedEmailConstraint`, `DuplicatedUsernameConstraint`, `DuplicatedArticleConstraint`)
- All validator implementations
- All param DTOs (`RegisterParam`, `UpdateUserParam`, `NewArticleParam`)

### Not Changed
- `javax.crypto.*` imports remain unchanged — these are JDK standard library packages, not Jakarta EE.

## 4. Spring Security Migration

### WebSecurityConfigurerAdapter Removed
- **Breaking Change**: `WebSecurityConfigurerAdapter` was removed in Spring Security 6 (shipped with Spring Boot 3).
- **Resolution**: Replaced with a `@Bean` method returning `SecurityFilterChain`.

### Method-Level Changes
| Old API | New API |
|---|---|
| `extends WebSecurityConfigurerAdapter` | Standalone `@Configuration` class |
| `override configure(HttpSecurity)` | `@Bean SecurityFilterChain securityFilterChain(HttpSecurity)` |
| `http.csrf().disable()` | `http.csrf(csrf -> csrf.disable())` |
| `http.cors().and()` | `http.cors(cors -> cors.configurationSource(...))` |
| `.authorizeRequests()` | `.authorizeHttpRequests()` |
| `.antMatchers(...)` | `.requestMatchers(...)` |
| `http.build()` not needed | `return http.build()` required |

### CORS Configuration
- `setAllowCredentials(true)` changed to `setAllowCredentials(false)` because wildcard origins (`*`) cannot be combined with credentials in the updated CORS spec enforcement.

## 5. ResponseEntityExceptionHandler Signature Change

### Breaking Change
Spring Framework 6 changed `handleMethodArgumentNotValid()` parameter from `HttpStatus` to `HttpStatusCode`.

### Resolution
Updated `CustomizeExceptionHandler.java`:
```java
// Old
protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status, WebRequest request)

// New  
protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request)
```

## 6. DGS GraphQL Framework Migration (4.x → 8.x)

### DataFetcherExceptionHandler API Change
- **Breaking Change**: `onException()` method removed from `DataFetcherExceptionHandler` interface. Replaced with `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`.
- **Resolution**: Updated `GraphQLCustomizeExceptionHandler` to implement `handleException()` and wrap results in `CompletableFuture.completedFuture(...)`.

### PageInfo Type Change
- **Breaking Change**: DGS 8.x codegen generates its own `io.spring.graphql.types.PageInfo` type instead of using `graphql.relay.PageInfo` from graphql-java.
- **Resolution**: Replaced `DefaultPageInfo` + `DefaultConnectionCursor` construction with the DGS-generated `PageInfo.newBuilder()` pattern in both `ArticleDatafetcher` and `CommentDatafetcher`.

## 7. JJWT API Migration (0.11.x → 0.12.x)

### Deprecated Builder Methods
| Old API | New API |
|---|---|
| `Jwts.builder().setSubject(...)` | `Jwts.builder().subject(...)` |
| `.setExpiration(...)` | `.expiration(...)` |
| `.signWith(key)` | `.signWith(key, Jwts.SIG.HS256)` |
| `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)` | `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` |

### Key Size Validation
- **Breaking Change**: jjwt 0.12.x enforces strict key-size validation when auto-detecting algorithms. The test secret (60 chars / 480 bits) was too short for HS512.
- **Resolution**: Switched from `HmacSHA512` to `HmacSHA256` and explicitly specified `Jwts.SIG.HS256` in the `signWith()` call to avoid auto-detection.

## 8. Selenium WebDriver API Change

### WebDriverWait Constructor
- **Breaking Change**: Selenium 4 (pulled in transitively) changed `WebDriverWait(driver, long)` to `WebDriverWait(driver, Duration)`.
- **Resolution**: Updated `BasePage.java` to use `Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS)`.

## 9. Actuator Configuration

Added Spring Boot Actuator for health monitoring:
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
```

Security configuration updated to permit `/actuator/**` endpoints without authentication.
