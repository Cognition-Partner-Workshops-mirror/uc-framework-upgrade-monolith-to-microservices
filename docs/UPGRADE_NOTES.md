# Spring Boot 2 → 3 Upgrade Notes

## Overview

This document records every breaking change encountered during the upgrade from Spring Boot 2.6.3 to Spring Boot 3.2.2 and how each was resolved.

## Build System Changes

| Change | Before | After | Resolution |
|--------|--------|-------|------------|
| Spring Boot version | 2.6.3 | 3.2.2 | Updated plugin version in `build.gradle` |
| Dependency Management | 1.0.11.RELEASE | 1.1.4 | Updated plugin version |
| Gradle version | 7.4 | 8.5 | Updated `gradle-wrapper.properties` |
| Java source/target | 11 | 17 | Updated `sourceCompatibility` and `targetCompatibility` |
| JaCoCo | 0.8.7 | 0.8.11 | Updated `toolVersion` |
| Spotless | 6.2.1 | 6.25.0 | Updated plugin version |

## Dependency Upgrades

| Dependency | Before | After | Notes |
|------------|--------|-------|-------|
| MyBatis Spring Boot | 2.2.2 | 3.0.3 | Jakarta namespace support |
| DGS Framework | 4.9.21 | 8.2.0 (BOM) | Major API changes |
| JJWT | 0.11.2 | 0.12.3 | Deprecated API removal |
| Rest-Assured | 4.5.1 | 5.4.0 | Jakarta compatibility |
| Mockito | 4.0.0 | 5.2.0 | Java 17 support |
| SQLite JDBC | 3.36.0.3 | 3.44.1.0 | Latest stable |
| Joda-Time | 2.10.13 | 2.12.5 | Latest stable |
| DGS Codegen Plugin | 5.0.6 | 6.0.3 | Compatible with DGS 8.x |

## Breaking Change: javax.* → jakarta.* Migration

**Problem:** Spring Boot 3 requires Jakarta EE 9+ namespaces.

**Files affected:** 21 Java files

**Resolution:** Replaced all `javax.validation.*`, `javax.servlet.*` imports with `jakarta.validation.*`, `jakarta.servlet.*`. Note: `javax.crypto.*` was left unchanged as it is part of the JDK, not Jakarta EE.

## Breaking Change: Spring Security Configuration

**Problem:** `WebSecurityConfigurerAdapter` was removed in Spring Security 6.

**Before:**
```java
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/articles/**")
            .permitAll();
    }
}
```

**After:**
```java
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth ->
                auth.requestMatchers(HttpMethod.GET, "/articles/**").permitAll());
        return http.build();
    }
}
```

**Key changes:**
- Replaced `WebSecurityConfigurerAdapter` with `@Bean SecurityFilterChain`
- Replaced `antMatchers()` with `requestMatchers()`
- Replaced `authorizeRequests()` with `authorizeHttpRequests()`
- Used lambda-based DSL for `csrf()`, `cors()`, `sessionManagement()`, `exceptionHandling()`

## Breaking Change: JJWT 0.12.x API

**Problem:** JJWT 0.12.x removed deprecated methods from 0.11.x.

**Changes:**
- `SignatureAlgorithm` enum removed → use `Keys.hmacShaKeyFor()` for automatic algorithm selection
- `Jwts.builder().setSubject()` → `Jwts.builder().subject()`
- `Jwts.builder().setExpiration()` → `Jwts.builder().expiration()`
- `Jwts.parserBuilder().setSigningKey().build().parseClaimsJws()` → `Jwts.parser().verifyWith().build().parseSignedClaims()`
- `claimsJws.getBody()` → `claimsJws.getPayload()`
- Minimum key size enforcement: keys must meet algorithm requirements (HS256=32B, HS384=48B, HS512=64B)

## Breaking Change: DGS Framework 8.x

**Problem:** DGS 8.x uses Spring for GraphQL integration and has API changes.

**Changes:**
- Artifact: `graphql-dgs-spring-boot-starter:4.9.21` → BOM `graphql-dgs-platform-dependencies:8.2.0` + `graphql-dgs-spring-boot-starter`
- `DataFetcherExceptionHandler.onException()` → `handleException()` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`
- `graphql.relay.PageInfo` type mismatch with DGS-generated `io.spring.graphql.types.PageInfo` → use generated type directly with builder pattern

## Breaking Change: ResponseEntityExceptionHandler Signature

**Problem:** In Spring 6, `handleMethodArgumentNotValid` parameter type changed.

**Before:** `HttpStatus status`
**After:** `HttpStatusCode status`

## Breaking Change: Selenium WebDriverWait Constructor

**Problem:** Selenium 4 deprecated the `long` timeout constructor.

**Resolution:** Changed from `new WebDriverWait(driver, timeoutSeconds)` to `new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))`.

## New Addition: Spring Boot Actuator

Added `spring-boot-starter-actuator` dependency to expose health check endpoints at `/actuator/health` for Docker Compose health monitoring.

## Configuration Changes

Added to `application.properties`:
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
spring.graphql.graphiql.enabled=true
spring.graphql.path=/graphql
```
