# Spring Boot 2 → 3 Upgrade Notes

## Overview

This document records every breaking change encountered during the upgrade of the RealWorld blogging platform from Spring Boot 2.6.3 to Spring Boot 3.2.5, and how each was resolved.

---

## 1. Gradle & Build Tool Changes

### 1.1 Gradle Version Upgrade
- **Breaking Change**: Spring Boot 3.2.5 requires Gradle 7.5+. The project used Gradle 7.4.
- **Resolution**: Updated `gradle/wrapper/gradle-wrapper.properties` from `gradle-7.4-bin.zip` to `gradle-8.7-bin.zip`.
- **File Changed**: `gradle/wrapper/gradle-wrapper.properties`

### 1.2 Spring Boot Plugin
- **Breaking Change**: Plugin version `2.6.3` → `3.2.5`
- **Resolution**: Updated `build.gradle` plugins block.
- **File Changed**: `build.gradle:2`

### 1.3 Spring Dependency Management Plugin
- **Breaking Change**: Version `1.0.11.RELEASE` incompatible with Spring Boot 3.
- **Resolution**: Updated to `1.1.5`.
- **File Changed**: `build.gradle:3`

### 1.4 Java Version
- **Breaking Change**: Spring Boot 3 requires Java 17+. Project was on Java 11.
- **Resolution**: Updated `sourceCompatibility` and `targetCompatibility` from `'11'` to `'17'`.
- **File Changed**: `build.gradle:11-12`

---

## 2. Jakarta EE Migration (javax → jakarta)

### 2.1 Overview
Spring Boot 3 adopts Jakarta EE 10, which renamed the `javax.*` namespace to `jakarta.*`. This affected **21 Java source files**.

### 2.2 Import Changes

| Old Import | New Import | Files Affected |
|-----------|-----------|---------------|
| `javax.validation.Valid` | `jakarta.validation.Valid` | ArticleApi, ArticlesApi, CommentsApi, UsersApi, CurrentUserApi |
| `javax.validation.constraints.*` | `jakarta.validation.constraints.*` | CommentsApi, RegisterParam, UpdateUserParam |
| `javax.validation.ConstraintViolation` | `jakarta.validation.ConstraintViolation` | CustomizeExceptionHandler, GraphQLCustomizeExceptionHandler |
| `javax.validation.ConstraintViolationException` | `jakarta.validation.ConstraintViolationException` | CustomizeExceptionHandler, GraphQLCustomizeExceptionHandler |
| `javax.validation.ConstraintValidator` | `jakarta.validation.ConstraintValidator` | DuplicatedArticleValidator, DuplicatedEmailValidator, DuplicatedUsernameValidator |
| `javax.validation.ConstraintValidatorContext` | `jakarta.validation.ConstraintValidatorContext` | All custom validators |
| `javax.validation.Constraint` | `jakarta.validation.Constraint` | DuplicatedArticleConstraint, DuplicatedEmailConstraint, DuplicatedUsernameConstraint |
| `javax.validation.Payload` | `jakarta.validation.Payload` | All custom constraint annotations |
| `javax.servlet.FilterChain` | `jakarta.servlet.FilterChain` | JwtTokenFilter |
| `javax.servlet.ServletException` | `jakarta.servlet.ServletException` | JwtTokenFilter |
| `javax.servlet.http.HttpServletRequest` | `jakarta.servlet.http.HttpServletRequest` | JwtTokenFilter |
| `javax.servlet.http.HttpServletResponse` | `jakarta.servlet.http.HttpServletResponse` | JwtTokenFilter |

### 2.3 Important: javax.crypto NOT Migrated
`javax.crypto.*` imports in `DefaultJwtService.java` were **not** migrated to `jakarta.crypto.*`. The `javax.crypto` package is part of the JDK itself (Java Cryptography Architecture), not Jakarta EE, and remains unchanged in Java 17.

---

## 3. Spring Security Changes

### 3.1 WebSecurityConfigurerAdapter Removal
- **Breaking Change**: `WebSecurityConfigurerAdapter` was deprecated in Spring Security 5.7 and **removed** in Spring Security 6.0 (used by Spring Boot 3).
- **Resolution**: Replaced class inheritance with component-based `SecurityFilterChain` `@Bean` pattern.
- **File Changed**: `WebSecurityConfig.java`

### 3.2 Security DSL Migration
- **Breaking Change**: Chain-style methods like `.csrf().disable()`, `.cors()`, `.authorizeRequests()` were deprecated in favor of lambda DSL.
- **Resolution**: Migrated to lambda DSL:
  - `.csrf().disable()` → `.csrf(AbstractHttpConfigurer::disable)`
  - `.cors().and()` → `.cors(cors -> cors.configurationSource(...))`
  - `.authorizeRequests()` → `.authorizeHttpRequests(auth -> ...)`
  - `.antMatchers(...)` → `.requestMatchers(...)`
- **File Changed**: `WebSecurityConfig.java`

### 3.3 ResponseEntityExceptionHandler Signature Change
- **Breaking Change**: `handleMethodArgumentNotValid()` method signature changed from `HttpStatus` to `HttpStatusCode` parameter in Spring 6.
- **Resolution**: Updated method signature in `CustomizeExceptionHandler.java`.
- **File Changed**: `CustomizeExceptionHandler.java:65-68`

---

## 4. JWT Library (jjwt) Upgrade

### 4.1 Version Change
- **Old**: jjwt 0.11.2
- **New**: jjwt 0.12.5

### 4.2 API Changes
| Old API (0.11.x) | New API (0.12.x) | Location |
|------------------|-----------------|----------|
| `SignatureAlgorithm.HS512` | Direct `"HmacSHA512"` string | `DefaultJwtService` constructor |
| `Jwts.builder().setSubject()` | `Jwts.builder().subject()` | `toToken()` method |
| `Jwts.builder().setExpiration()` | `Jwts.builder().expiration()` | `toToken()` method |
| `Jwts.parserBuilder().setSigningKey()` | `Jwts.parser().verifyWith()` | `getSubFromToken()` method |
| `.parseClaimsJws()` | `.parseSignedClaims()` | `getSubFromToken()` method |
| `claimsJws.getBody()` | `claimsJws.getPayload()` | `getSubFromToken()` method |

### 4.3 Key Length Requirement
- **Breaking Change**: jjwt 0.12.x enforces minimum key lengths per algorithm. HS512 requires ≥64 bytes.
- **Resolution**: Updated test secret from 60 to 88 bytes in `DefaultJwtServiceTest.java`.
- **File Changed**: `DefaultJwtServiceTest.java:17`

---

## 5. Netflix DGS Framework Upgrade

### 5.1 Version Changes
| Component | Old | New |
|----------|-----|-----|
| DGS codegen plugin | 5.0.6 | 6.0.3 |
| DGS Spring Boot starter | 4.9.21 | 8.5.0 |

### 5.2 PageInfo Type Incompatibility
- **Breaking Change**: DGS 8.x codegen generates its own `io.spring.graphql.types.PageInfo` type that is incompatible with `graphql.relay.DefaultPageInfo`.
- **Resolution**: Replaced `graphql.relay.DefaultPageInfo` construction with `io.spring.graphql.types.PageInfo.newBuilder()` pattern in both `ArticleDatafetcher.java` and `CommentDatafetcher.java`.
- **Files Changed**: `ArticleDatafetcher.java`, `CommentDatafetcher.java`

### 5.3 DataFetcherExceptionHandler Interface Change
- **Breaking Change**: `DataFetcherExceptionHandler.onException()` replaced with `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>` in graphql-java 21+.
- **Resolution**: Updated `GraphQLCustomizeExceptionHandler` to implement `handleException()` with `CompletableFuture.completedFuture()` wrapping.
- **File Changed**: `GraphQLCustomizeExceptionHandler.java`

---

## 6. Other Dependency Upgrades

| Dependency | Old Version | New Version | Reason |
|-----------|------------|------------|--------|
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 | Spring Boot 3 compatibility |
| SQLite JDBC | 3.36.0.3 | 3.45.3.0 | Latest stable, security fixes |
| Joda-Time | 2.10.13 | 2.12.7 | Latest stable |
| Rest Assured | 4.5.1 | 5.4.0 | Jakarta EE servlet API compatibility |
| Mockito Inline | 4.0.0 | 5.2.0 | Java 17 compatibility |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 bytecode support |
| Spotless | 6.2.1 | 6.25.0 | Gradle 8 compatibility |

### 6.1 Spring Boot Actuator Added
- **New Dependency**: `spring-boot-starter-actuator` added for health check endpoints.
- **Configuration**: Exposed `health` and `info` endpoints via `management.endpoints.web.exposure.include`.
- **Security**: Added `/actuator/**` to permitted URL patterns in `WebSecurityConfig.java`.

---

## 7. Selenium Test Fix

### 7.1 WebDriverWait Duration
- **Breaking Change**: Selenium 4.x changed `WebDriverWait` constructor from accepting `long` seconds to `java.time.Duration`.
- **Resolution**: Changed `new WebDriverWait(driver, DEFAULT_TIMEOUT_SECONDS)` to `new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))`.
- **File Changed**: `BasePage.java:18-20`

---

## 8. Test Results

After all changes, the full test suite passes:
- **68 tests** executed
- **68 tests** passed
- **0 failures**

```
BUILD SUCCESSFUL
68 tests completed, 0 failed
```
