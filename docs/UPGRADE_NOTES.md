# Upgrade Notes: Spring Boot 2.6.3 → 3.2.5

This document details every breaking change encountered during the framework upgrade.

## 1. Java 11 → 17

Required by Spring Boot 3 / Spring Framework 6. Updated `sourceCompatibility` and `targetCompatibility` in `build.gradle` and `.java-version` file.

## 2. javax.* → jakarta.* Namespace Migration

21 files affected across two categories:

- **javax.servlet → jakarta.servlet** (1 file): `JwtTokenFilter.java` — `FilterChain`, `ServletException`, `HttpServletRequest`, `HttpServletResponse`
- **javax.validation → jakarta.validation** (18+ files): All API controllers, command services, validator constraints, and GraphQL exception handlers
- **javax.crypto unchanged**: `DefaultJwtService.java` uses `javax.crypto.SecretKey` and `javax.crypto.spec.SecretKeySpec` — these are JDK packages (not Jakarta EE) and remain as-is

## 3. WebSecurityConfigurerAdapter Removed

Migrated from `extends WebSecurityConfigurerAdapter` with `configure(HttpSecurity)` override to the `SecurityFilterChain` bean pattern with lambda DSL:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // ...
    return http.build();
}
```

## 4. antMatchers() → requestMatchers()

Spring Security 6 renamed `antMatchers()` to `requestMatchers()` and `authorizeRequests()` to `authorizeHttpRequests()`. All security configuration updated accordingly.

## 5. JJWT 0.11 → 0.12

Builder and parser API changes in JJWT 0.12.x:

| Old (0.11.x) | New (0.12.x) |
|---|---|
| `Jwts.builder().setSubject(...)` | `Jwts.builder().subject(...)` |
| `.setExpiration(...)` | `.expiration(...)` |
| `SignatureAlgorithm.HS512` | `Jwts.SIG.HS512` |
| `Jwts.parserBuilder().setSigningKey(key).build()` | `Jwts.parser().verifyWith(key).build()` |
| `.parseClaimsJws(token)` | `.parseSignedClaims(token)` |
| `.getBody().getSubject()` | `.getPayload().getSubject()` |

The signing key must be ≥64 bytes for HS512. The JWT secret is sourced from `JWT_SECRET` environment variable with a non-secret dev placeholder fallback.

## 6. handleMethodArgumentNotValid Signature

Spring Framework 6 changed the `ResponseEntityExceptionHandler.handleMethodArgumentNotValid` parameter from `HttpStatus` to `HttpStatusCode`:

```java
// Before
protected ResponseEntity<Object> handleMethodArgumentNotValid(..., HttpStatus status, ...)
// After
protected ResponseEntity<Object> handleMethodArgumentNotValid(..., HttpStatusCode status, ...)
```

## 7. Netflix DGS 4.x → 8.x

Required for Spring Boot 3 compatibility. DGS 8.x uses `graphql-java` 21+ where `DataFetcherExceptionHandler.onException()` returns `CompletableFuture<DataFetcherExceptionHandlerResult>`. Updated `GraphQLCustomizeExceptionHandler` accordingly. DGS codegen plugin updated from `5.0.6` to `6.2.1`.

## 8. MyBatis Spring Boot Starter 2.x → 3.x

Updated from `2.2.2` to `3.0.3` for Jakarta namespace support. No API changes required in mapper interfaces or XML files.

## 9. Spring Dependency Management Plugin

Updated from `1.0.11.RELEASE` to `1.1.5` to align with Spring Boot 3.2.x BOM management.

## 10. rest-assured 4.x → 5.x

Updated from `4.5.1` to `5.4.0` for Jakarta servlet support in `spring-mock-mvc` module. No test code changes required.

## 11. mockito-inline → mockito-core 5.x

Updated from `mockito-inline:4.0.0` to `mockito-inline:5.2.0`. In Mockito 5, inline mocking is the default behavior merged into core, but we keep the explicit dependency for compatibility.

## 12. Additional Dependency Updates

| Dependency | Old Version | New Version |
|---|---|---|
| `sqlite-jdbc` | 3.36.0.3 | 3.45.3.0 |
| `spotless` plugin | 6.2.1 | 6.25.0 |
| `jacoco` toolVersion | 0.8.7 | 0.8.12 |
| Gradle wrapper | (original) | 8.7 |

## 13. Gradle Wrapper

Updated `gradle/wrapper/gradle-wrapper.properties` to use Gradle 8.7, which is required for compatibility with Spring Boot 3.2.x and the updated plugin ecosystem.
