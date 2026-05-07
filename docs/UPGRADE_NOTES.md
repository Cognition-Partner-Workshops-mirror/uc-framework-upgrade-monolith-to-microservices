# Spring Boot 2 → 3 Upgrade Notes

This document records every breaking change encountered during the upgrade from Spring Boot 2.6.3 to 3.2.4 and how each was resolved.

## 1. Core Framework Versions

| Component | Before | After |
|-----------|--------|-------|
| Spring Boot | 2.6.3 | 3.2.4 |
| Java | 11 | 17 |
| Gradle | 7.4 | 8.6 |
| Spring Framework | 5.x | 6.x |
| Spring Security | 5.x | 6.x |

## 2. Jakarta EE Migration (javax → jakarta)

**Breaking Change:** Spring Boot 3 requires Jakarta EE 10, which uses the `jakarta.*` namespace instead of `javax.*`.

**Resolution:** Migrated all imports across 20+ source files:
- `javax.validation.*` → `jakarta.validation.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.persistence.*` → `jakarta.persistence.*` (if used)

**Note:** `javax.crypto.*` was intentionally NOT changed — it is part of the JDK, not Jakarta EE.

**Files affected:** All controllers, validators, filters, exception handlers, and service classes that used bean validation or servlet APIs.

## 3. Spring Security: WebSecurityConfigurerAdapter Removed

**Breaking Change:** `WebSecurityConfigurerAdapter` was removed in Spring Security 6. The `configure(HttpSecurity)` override pattern no longer works.

**Resolution:** Refactored `WebSecurityConfig.java` to use the new `SecurityFilterChain` bean pattern:
- Removed `extends WebSecurityConfigurerAdapter`
- Created a `@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)` method
- Migrated to lambda DSL: `.csrf(AbstractHttpConfigurer::disable)`, `.authorizeHttpRequests(auth -> ...)`
- Replaced deprecated `antMatchers()` with `requestMatchers()`

## 4. JJWT 0.11.x → 0.12.x API Changes

**Breaking Change:** JJWT 0.12.x introduced breaking API changes and enforces minimum key sizes.

**Resolution in `DefaultJwtService.java`:**
- `SignatureAlgorithm` enum removed → use `"HmacSHA512"` string for `SecretKeySpec`
- `Jwts.builder().setSubject()` → `Jwts.builder().subject()`
- `Jwts.builder().setExpiration()` → `Jwts.builder().expiration()`
- `Jwts.parserBuilder()` → `Jwts.parser()`
- `parseClaimsJws()` → `parseSignedClaims()`
- `getBody()` → `getPayload()`

**Resolution in tests:** Updated test secret key to meet JJWT 0.12.x minimum key length requirement for HS512 (64 bytes).

## 5. Spring Framework 6: Exception Handler Signature Change

**Breaking Change:** `ResponseEntityExceptionHandler.handleMethodArgumentNotValid()` changed its parameter type from `HttpStatus` to `HttpStatusCode`.

**Resolution in `CustomizeExceptionHandler.java`:**
```java
// Before (Spring Boot 2):
protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException e, HttpHeaders headers,
    HttpStatus status, WebRequest request)

// After (Spring Boot 3):
protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException e, HttpHeaders headers,
    HttpStatusCode status, WebRequest request)
```

## 6. Netflix DGS 4.x → 7.x

**Breaking Change:** DGS 7.x updated the GraphQL Java types and the `DataFetcherExceptionHandler` interface.

**Resolution:**
- **PageInfo type conflict:** `graphql.relay.DefaultPageInfo` no longer compatible with DGS-generated `io.spring.graphql.types.PageInfo`. Replaced `DefaultPageInfo` usage with the DGS-generated `PageInfo.newBuilder()` pattern in both `ArticleDatafetcher` and `CommentDatafetcher`.
- **Exception handler:** `DataFetcherExceptionHandler.onException()` became `handleException()` and now returns `CompletableFuture<DataFetcherExceptionHandlerResult>` instead of synchronous `DataFetcherExceptionHandlerResult`. Updated `GraphQLCustomizeExceptionHandler` accordingly.

## 7. MyBatis 2.x → 3.x

**Breaking Change:** MyBatis Spring Boot Starter 2.x is not compatible with Spring Boot 3 / Jakarta EE.

**Resolution:** Upgraded `mybatis-spring-boot-starter` from 2.2.2 to 3.0.3 and `mybatis-spring-boot-starter-test` accordingly. No code changes required — MyBatis 3.x transparently handles the Jakarta namespace.

## 8. Rest Assured 4.x → 5.x

**Breaking Change:** Rest Assured 4.x uses `javax.servlet` which conflicts with Spring Boot 3's Jakarta EE requirement.

**Resolution:** Upgraded `rest-assured` from 4.x to 5.4.0 which supports Jakarta EE. No test code changes required.

## 9. Selenium WebDriverWait API

**Breaking Change:** Selenium 4 (pulled in by Spring Boot 3 test dependencies) changed `WebDriverWait` constructor to accept `Duration` instead of `long`.

**Resolution in `BasePage.java`:**
```java
// Before:
this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT_SECONDS);

// After:
this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
```

## 10. DGS Codegen Plugin

**Breaking Change:** DGS Codegen 5.x is not compatible with Spring Boot 3.

**Resolution:** Upgraded `com.netflix.dgs.codegen` Gradle plugin from 5.x to 6.0.3 for Spring Boot 3 compatibility.

## 11. Spotless Plugin

**Breaking Change:** Spotless 6.x requires Java 11+, but for full compatibility with Java 17 source level features, a newer version is recommended.

**Resolution:** Upgraded `com.diffplug.spotless` from earlier version to 6.25.0.

## 12. Spring Boot Actuator

**Addition:** Added `spring-boot-starter-actuator` dependency and configured health endpoints for production readiness and Docker health checks.

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
```
