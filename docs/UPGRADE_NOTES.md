# Spring Boot 2 → 3 Upgrade Notes

This document captures every breaking change encountered during the upgrade from Spring Boot 2.6.3 to 3.2.0 and how each was resolved.

## 1. Build System Changes

### Gradle Wrapper: 7.4 → 8.5
- **Breaking Change**: Spring Boot 3.x requires Gradle 7.5+ (recommended 8.x).
- **Resolution**: Updated `gradle/wrapper/gradle-wrapper.properties` to use Gradle 8.5 distribution.

### Spring Boot Plugin: 2.6.3 → 3.2.0
- **Breaking Change**: The `org.springframework.boot` plugin version must match the Spring Boot version.
- **Resolution**: Updated `build.gradle` plugin version from `2.6.3` to `3.2.0`.

### Dependency Management Plugin: 1.0.11.RELEASE → 1.1.4
- **Breaking Change**: Older dependency management plugin versions are incompatible with Spring Boot 3.x.
- **Resolution**: Updated `io.spring.dependency-management` from `1.0.11.RELEASE` to `1.1.4`.

### Java Version: 11 → 17
- **Breaking Change**: Spring Boot 3.x requires Java 17 as the minimum version.
- **Resolution**: Updated `sourceCompatibility` and `targetCompatibility` from `11` to `17` in `build.gradle`, and updated `.java-version` file.

## 2. Jakarta EE Namespace Migration (javax → jakarta)

### Overview
Spring Boot 3.x is built on Jakarta EE 10, which renames all `javax.*` packages to `jakarta.*`. This is the single largest breaking change in the upgrade.

### javax.validation → jakarta.validation (20+ files)
- **Affected files**: All files using Bean Validation annotations (`@Valid`, `@NotBlank`, `@Email`, `@Constraint`, `ConstraintValidator`, `ConstraintViolation`, `ConstraintViolationException`).
- **Resolution**: Global find-and-replace across all source files:
  - `import javax.validation.Valid` → `import jakarta.validation.Valid`
  - `import javax.validation.constraints.*` → `import jakarta.validation.constraints.*`
  - `import javax.validation.ConstraintValidator` → `import jakarta.validation.ConstraintValidator`
  - And all other `javax.validation` imports.

### javax.servlet → jakarta.servlet (JwtTokenFilter)
- **Affected files**: `JwtTokenFilter.java`
- **Resolution**: Changed imports:
  - `import javax.servlet.FilterChain` → `import jakarta.servlet.FilterChain`
  - `import javax.servlet.ServletException` → `import jakarta.servlet.ServletException`
  - `import javax.servlet.http.HttpServletRequest` → `import jakarta.servlet.http.HttpServletRequest`
  - `import javax.servlet.http.HttpServletResponse` → `import jakarta.servlet.http.HttpServletResponse`

### javax.crypto (NO CHANGE)
- **Important**: `javax.crypto.*` is part of the Java SE standard library (JCE), NOT Jakarta EE. These imports remain unchanged.
- **Affected files**: `DefaultJwtService.java` — `javax.crypto.SecretKey` and `javax.crypto.spec.SecretKeySpec` stay as-is.

## 3. Spring Security Changes

### WebSecurityConfigurerAdapter → SecurityFilterChain
- **Breaking Change**: `WebSecurityConfigurerAdapter` was deprecated in Spring Security 5.7 and removed entirely in Spring Security 6.0 (shipped with Spring Boot 3.x).
- **Resolution**: Rewrote `WebSecurityConfig.java`:
  - Removed `extends WebSecurityConfigurerAdapter`
  - Replaced `configure(HttpSecurity http)` override with a `@Bean` method returning `SecurityFilterChain`
  - Changed `http.csrf().disable().cors()...` (chaining) to lambda-based configuration: `http.csrf(csrf -> csrf.disable()).cors(cors -> ...)`

### authorizeRequests() → authorizeHttpRequests()
- **Breaking Change**: `authorizeRequests()` is deprecated; `authorizeHttpRequests()` is the replacement.
- **Resolution**: Updated all security configuration to use `authorizeHttpRequests()`.

### antMatchers() → requestMatchers()
- **Breaking Change**: `antMatchers()` was removed in Spring Security 6.0.
- **Resolution**: Replaced all `antMatchers(...)` calls with `requestMatchers(...)`.

## 4. Spring Framework 6 Changes

### ResponseEntityExceptionHandler Method Signature
- **Breaking Change**: In Spring Framework 6, `handleMethodArgumentNotValid()` override signature changed — the `status` parameter type changed from `HttpStatus` to `HttpStatusCode`.
- **Resolution**: Updated `CustomizeExceptionHandler.java` to use `HttpStatusCode` instead of `HttpStatus` in the method signature.

## 5. JJWT Library Upgrade: 0.11.2 → 0.12.3

### Builder API Changes
- **Breaking Change**: `setSubject()`, `setExpiration()` renamed to `subject()`, `expiration()`.
- **Resolution**: Updated `DefaultJwtService.toToken()` to use new method names.

### signWith() Requires Explicit Algorithm
- **Breaking Change**: `signWith(key)` no longer auto-detects the algorithm from key size. If the key is too small for the default algorithm, it throws `UnsupportedKeyException`.
- **Resolution**: Changed to `signWith(key, algorithm)` with explicit `MacAlgorithm` selection based on key length (HS256/HS384/HS512). Used `Keys.hmacShaKeyFor()` for proper key derivation.

### Parser API Changes
- **Breaking Change**: `Jwts.parserBuilder().setSigningKey()` replaced with `Jwts.parser().verifyWith()`. `parseClaimsJws()` replaced with `parseSignedClaims()`. `getBody()` replaced with `getPayload()`.
- **Resolution**: Updated `DefaultJwtService.getSubFromToken()` to use the new API chain.

### SignatureAlgorithm Enum Removed
- **Breaking Change**: `io.jsonwebtoken.SignatureAlgorithm` enum was removed in JJWT 0.12.x.
- **Resolution**: Replaced with `Jwts.SIG.HS256` / `Jwts.SIG.HS384` / `Jwts.SIG.HS512` constants.

## 6. Netflix DGS GraphQL Framework: 4.9.21 → 8.1.1

### PageInfo Type Incompatibility
- **Breaking Change**: `graphql.relay.DefaultPageInfo` is no longer compatible with the generated `io.spring.graphql.types.PageInfo` in newer DGS versions.
- **Resolution**: Replaced `DefaultPageInfo` and `DefaultConnectionCursor` with the DGS-generated `PageInfo.newBuilder()` pattern in both `ArticleDatafetcher.java` and `CommentDatafetcher.java`.

### DataFetcherExceptionHandler Interface Change
- **Breaking Change**: `DataFetcherExceptionHandler.onException()` was replaced with `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>` in graphql-java 21.x.
- **Resolution**: Updated `GraphQLCustomizeExceptionHandler` to implement the new `handleException()` method with `CompletableFuture` return type.

### DGS Codegen Plugin: 5.0.6 → 6.0.3
- **Breaking Change**: Older codegen plugin versions are incompatible with Spring Boot 3.x.
- **Resolution**: Updated the `com.netflix.dgs.codegen` plugin version.

## 7. Other Dependency Upgrades

| Dependency | Old Version | New Version | Reason |
|---|---|---|---|
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 | Jakarta namespace support |
| REST Assured | 4.5.1 | 5.4.0 | Jakarta namespace support |
| SQLite JDBC | 3.36.0.3 | 3.44.1.0 | Java 17 compatibility |
| Joda-Time | 2.10.13 | 2.12.5 | Latest stable |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 bytecode support |
| Spotless | 6.2.1 | 6.25.0 | Java 17 formatting support |

## 8. Selenium Test Fix

### WebDriverWait Constructor Change
- **Breaking Change**: In Selenium 4.x, `WebDriverWait(driver, long)` was removed. The constructor now requires `Duration` instead of a raw `long` for the timeout.
- **Resolution**: Changed `new WebDriverWait(driver, DEFAULT_TIMEOUT_SECONDS)` to `new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))` in `BasePage.java`.

## 9. New Additions

### Spring Boot Actuator
- **Added**: `spring-boot-starter-actuator` dependency to expose `/actuator/health` endpoint.
- **Configuration**: `management.endpoints.web.exposure.include=health,info` in `application.properties`.
- **Purpose**: Required for Docker Compose health checks and service monitoring.
