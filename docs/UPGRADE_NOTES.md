# Spring Boot 2.x to 3.x Upgrade Notes

## Overview

Upgraded the RealWorld blogging platform monolith from Spring Boot 2.6.3 to 3.2.5, including all required dependency and code migrations.

---

## Breaking Changes and Resolutions

### 1. Java Version (11 → 17)

**Change:** Spring Boot 3.x requires Java 17 as the minimum baseline.

**Resolution:** Updated `.java-version` from `11` to `17` and set `sourceCompatibility`/`targetCompatibility` to `17` in `build.gradle`.

---

### 2. Jakarta EE Migration (javax.* → jakarta.*)

**Change:** Spring Boot 3 / Spring Framework 6 moved from Java EE (`javax.*`) to Jakarta EE (`jakarta.*`) namespaces. All `javax.validation`, `javax.servlet`, and `javax.persistence` imports are no longer resolved.

**Resolution:** Batch-migrated all imports across the codebase:
- `javax.validation.*` → `jakarta.validation.*` (constraint annotations, `ConstraintViolation`, `ConstraintValidatorContext`, etc.)
- `javax.servlet.*` → `jakarta.servlet.*` (`HttpServletRequest`, `FilterChain`, etc.)

**Exception:** `javax.crypto.*` imports were left unchanged — these are part of the JDK, not Jakarta EE.

**Affected files (25+):** All API controllers, security filters, validation constraints, custom validators, and the exception handler.

---

### 3. Spring Security Configuration

**Change:** `WebSecurityConfigurerAdapter` was removed in Spring Security 6.x. The `authorizeRequests()` / `antMatchers()` DSL was replaced.

**Resolution:** Refactored `WebSecurityConfig.java`:
- Removed `extends WebSecurityConfigurerAdapter`
- Created a `@Bean SecurityFilterChain securityFilterChain(HttpSecurity http)` method
- Migrated to lambda DSL: `csrf(AbstractHttpConfigurer::disable)`, `cors(cors -> ...)`, `sessionManagement(session -> ...)`
- Replaced `antMatchers()` with `requestMatchers()`
- Replaced `authorizeRequests()` with `authorizeHttpRequests()`
- Added `/actuator/**` to permitted paths for health check access

---

### 4. JJWT Library Upgrade (0.11.2 → 0.12.6)

**Change:** JJWT 0.12.x introduced breaking API changes:
- `SignatureAlgorithm` enum removed from `signWith()`
- `setSubject()` / `setExpiration()` replaced by `subject()` / `expiration()`
- `Jwts.parserBuilder()` replaced by `Jwts.parser()`
- `setSigningKey()` replaced by `verifyWith()`
- `parseClaimsJws()` replaced by `parseSignedClaims()`
- `getBody()` replaced by `getPayload()`

**Resolution:** Updated `DefaultJwtService.java`:
```java
// Key creation
this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());

// Token building
Jwts.builder().subject(user.getId()).expiration(expireTimeFromNow()).signWith(signingKey).compact();

// Token parsing
Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload().getSubject();
```

---

### 5. Netflix DGS Framework (4.9.21 → 8.5.0)

**Change:** DGS 8.x updated the `DataFetcherExceptionHandler` interface. The `onException()` method (returning `DataFetcherExceptionHandlerResult`) was replaced by `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`.

**Resolution:** Updated `GraphQLCustomizeExceptionHandler.java`:
- Changed method signature to `CompletableFuture<DataFetcherExceptionHandlerResult> handleException(...)`
- Wrapped all return values in `CompletableFuture.completedFuture(...)`
- Removed the `DefaultDataFetcherExceptionHandler` field (no longer needed)

---

### 6. GraphQL PageInfo Type Mismatch

**Change:** With DGS 8.x code generation, `io.spring.graphql.types.PageInfo` is generated from the schema. The old code used `graphql.relay.DefaultPageInfo` / `DefaultConnectionCursor` which is incompatible with the generated builder type.

**Resolution:** Updated `ArticleDatafetcher.java` and `CommentDatafetcher.java`:
- Removed `graphql.relay.DefaultPageInfo` and `DefaultConnectionCursor` imports
- Used `PageInfo.newBuilder().startCursor(...).endCursor(...).hasNextPage(...).hasPreviousPage(...).build()` from the generated types

---

### 7. Spring GraphQL Schema Inspection

**Change:** Spring Boot 3.2 introduced `SchemaMappingInspector` which validates GraphQL schema types at startup. It doesn't understand DGS's custom connection types (e.g., `ArticlesConnection`), causing: `IllegalStateException: No node type for 'ArticlesConnection'`.

**Resolution:** Disabled the inspection in `application.properties`:
```properties
spring.graphql.schema.inspection.enabled=false
```

---

### 8. ResponseEntityExceptionHandler Signature Change

**Change:** In Spring Framework 6, `handleMethodArgumentNotValid()` changed its `status` parameter type from `HttpStatus` to `HttpStatusCode`.

**Resolution:** Updated `CustomizeExceptionHandler.java` to use `HttpStatusCode` instead of `HttpStatus`.

---

### 9. Selenium WebDriverWait API

**Change:** Selenium 4.x deprecated the `WebDriverWait(WebDriver, long)` constructor in favor of `WebDriverWait(WebDriver, Duration)`.

**Resolution:** Updated `BasePage.java` in test sources:
```java
this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
```

---

### 10. Gradle and Dependency Version Updates

| Dependency | Old Version | New Version |
|---|---|---|
| Spring Boot | 2.6.3 | 3.2.5 |
| Dependency Management Plugin | 1.0.11.RELEASE | 1.1.5 |
| Gradle Wrapper | 7.4 | 8.5 |
| Netflix DGS | 4.9.21 | 8.5.0 |
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 |
| JJWT | 0.11.2 | 0.12.6 |
| Flyway | 8.5.13 | 9.22.3 |
| Spotless | 6.3.0 | 6.25.0 |
| JaCoCo | 0.8.7 | 0.8.11 |

---

## Test Results

All 68 existing tests pass after the upgrade. The JaCoCo coverage verification threshold (80%) was already not met before the upgrade — this is a pre-existing condition unrelated to the migration.
