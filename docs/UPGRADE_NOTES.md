# Upgrade Notes: Spring Boot 2.6.3 → 3.2.5

This document records every breaking change encountered during the Spring Boot 2 → 3 upgrade and how each was resolved.

## 1. Build & Dependency Changes

| Dependency | Old Version | New Version | Reason |
|---|---|---|---|
| Spring Boot | 2.6.3 | 3.2.5 | Jakarta EE 10 + Spring Framework 6 |
| Gradle wrapper | 7.4 | 8.7 | Required by Spring Boot 3 plugin |
| Java source/target | 11 | 17 | Minimum for Spring Boot 3 |
| Spring dependency-management plugin | 1.0.11.RELEASE | 1.1.4 | Spring Boot 3 compatibility |
| MyBatis Spring Boot Starter | 2.2.2 | 3.0.3 | Jakarta namespace support |
| Netflix DGS | 4.9.21 | 8.5.0 (BOM) | Spring Boot 3 / Spring GraphQL integration |
| DGS Codegen plugin | 5.0.6 | 6.1.0 | Spring Boot 3 codegen support |
| JJWT | 0.11.2 | 0.12.5 | Jakarta namespace + new API |
| REST Assured | 4.5.1 | 5.4.0 | Jakarta namespace |
| SQLite JDBC | 3.36.0.3 | 3.45.2.0 | Java 17 compatibility |
| Joda Time | 2.10.13 | 2.12.7 | Bug fixes |
| JaCoCo | 0.8.7 | 0.8.11 | Java 17 bytecode support |
| Spotless | 6.2.1 | 6.25.0 | Java 17 compatibility |

**New dependency added:** `spring-boot-starter-actuator` for health check endpoints.

**DGS dependency change:** Replaced single `graphql-dgs-spring-boot-starter` with `graphql-dgs-platform-dependencies` BOM + `graphql-dgs-spring-graphql-starter` (DGS 8 uses Spring GraphQL under the hood).

## 2. Jakarta Namespace Migration (javax → jakarta)

### Breaking Change
Spring Boot 3 requires Jakarta EE 10, which renamed all `javax.*` packages to `jakarta.*`.

### Resolution
- `javax.validation.*` → `jakarta.validation.*` (20 files affected)
- `javax.servlet.*` → `jakarta.servlet.*` (JwtTokenFilter)
- `javax.crypto.*` was **NOT** changed — it's part of Java SE, not Jakarta EE

### Affected Files
- All REST controllers with `@Valid` annotations
- All validation constraint annotations and validators
- `JwtTokenFilter` (servlet filter)
- `GraphQLCustomizeExceptionHandler` (ConstraintViolationException)
- `CustomizeExceptionHandler` (ConstraintViolationException)

## 3. Spring Security Migration

### Breaking Change
`WebSecurityConfigurerAdapter` was removed in Spring Security 6 (Spring Boot 3).

### Resolution
Replaced the `extends WebSecurityConfigurerAdapter` pattern with a `@Bean SecurityFilterChain` method.

**API changes applied:**
| Old (Spring Security 5) | New (Spring Security 6) |
|---|---|
| `extends WebSecurityConfigurerAdapter` | `@Bean SecurityFilterChain` method |
| `configure(HttpSecurity http)` | `securityFilterChain(HttpSecurity http)` |
| `.antMatchers()` | `.requestMatchers()` |
| `.authorizeRequests()` | `.authorizeHttpRequests()` |
| `.csrf().disable()` | `.csrf(AbstractHttpConfigurer::disable)` |
| `.cors()` | `.cors(cors -> cors.configurationSource(...))` |
| `.exceptionHandling().authenticationEntryPoint(...)` | `.exceptionHandling(e -> e.authenticationEntryPoint(...))` |
| `.sessionManagement().sessionCreationPolicy(...)` | `.sessionManagement(s -> s.sessionCreationPolicy(...))` |

### Additional Security Changes
- Added `.requestMatchers("/actuator/**").permitAll()` for health endpoints
- The `PasswordEncoder` bean was moved to the config class
- `JwtTokenFilter` bean created via `@Bean` method

## 4. JJWT 0.11 → 0.12 Migration

### Breaking Change
JJWT 0.12.x removed deprecated builder methods and renamed parser methods.

### Resolution
| Old (0.11.x) | New (0.12.x) |
|---|---|
| `Jwts.builder().setSubject(...)` | `Jwts.builder().subject(...)` |
| `.setExpiration(...)` | `.expiration(...)` |
| `SignatureAlgorithm.HS512` enum | Removed — algorithm inferred from key |
| `Jwts.parserBuilder().setSigningKey(key).build()` | `Jwts.parser().verifyWith(key).build()` |
| `.parseClaimsJws(token)` | `.parseSignedClaims(token)` |
| `claims.getBody()` | `claims.getPayload()` |

### Key Size Enforcement
JJWT 0.12 enforces minimum key sizes: HS512 requires ≥ 64 bytes (512 bits). The test secret was updated to meet this requirement.

## 5. DGS 4.9 → 8.5 Migration

### Breaking Change 1: Exception Handler
`DataFetcherExceptionHandler.onException()` was removed. The new method is `handleException()` which returns `CompletableFuture<DataFetcherExceptionHandlerResult>`.

### Breaking Change 2: PageInfo Type
DGS codegen now generates `io.spring.graphql.types.PageInfo` instead of using `graphql.relay.PageInfo`. All usages of `DefaultPageInfo` and `DefaultConnectionCursor` were replaced with the generated `PageInfo.newBuilder()` pattern.

### Breaking Change 3: Spring GraphQL Integration
DGS 8 uses Spring GraphQL internally. Required additional configuration:
```properties
spring.graphql.graphiql.enabled=true
spring.graphql.schema.locations=classpath:schema/
spring.graphql.schema.inspection.enabled=false
```
The `inspection.enabled=false` setting prevents Spring GraphQL's `ConnectionTypeDefinitionConfigurer` from conflicting with DGS-managed connection types.

## 6. Spring Framework 6 API Changes

### ResponseEntityExceptionHandler
`handleMethodArgumentNotValid()` signature changed:
- **Old:** `HttpStatus status` parameter
- **New:** `HttpStatusCode status` parameter

This broke `CustomizeExceptionHandler` and was fixed by updating the parameter type.

## 7. Selenium 4 API Changes

### WebDriverWait Constructor
- **Old:** `new WebDriverWait(driver, long seconds)`
- **New:** `new WebDriverWait(driver, Duration.ofSeconds(seconds))`

## 8. Test Fixes

### Spring Boot 3 Test Context
`@SpringBootTest` with default `webEnvironment = MOCK` failed to create the `mvcHandlerMappingIntrospector` bean due to Spring Security 6's new requirements. Fixed by using `webEnvironment = RANDOM_PORT` on affected tests:
- `RealworldApplicationTests`
- `ArticleRepositoryTransactionTest`

### Final Test Results
All 68 tests pass after the upgrade.
