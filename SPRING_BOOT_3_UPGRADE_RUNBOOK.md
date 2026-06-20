# Spring Boot 2.x → 3.x Upgrade Runbook

A repeatable, step-by-step checklist for upgrading any Spring Boot 2.x application to Spring Boot 3.x (Java 17+). Use this as a shared reference across teams and projects.

---

## Pre-Flight Checklist

- [ ] **Inventory your dependencies** — List every dependency and its current version. Check each for a Jakarta EE 10 / Spring Boot 3.x compatible release. Flag any that don't have one yet.
- [ ] **Review Spring Boot 3.0 Migration Guide** — https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
- [ ] **Run the OpenRewrite migration recipes (optional)** — `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_2` can automate some changes. Review output carefully.
- [ ] **Create a feature branch** — Never upgrade on `main` directly.
- [ ] **Ensure test coverage is adequate** — Run existing tests on the current version first and record the baseline pass/fail count.

---

## Phase 1: Build System & Java Version

### 1.1 Upgrade Java
- [ ] Update `.java-version` (or equivalent) from 11 → 17 (minimum for Spring Boot 3.x)
- [ ] Update `build.gradle` or `pom.xml`: `sourceCompatibility = '17'`, `targetCompatibility = '17'`
- [ ] Verify your CI/CD pipeline uses Java 17+ JDK

### 1.2 Upgrade Build Tool
- [ ] **Gradle**: Upgrade wrapper to 8.5+ (`./gradlew wrapper --gradle-version 8.5`)
  - Gradle 7.x does not fully support Java 17 toolchains
- [ ] **Maven**: Upgrade to 3.8+ (should already work with Java 17)

### 1.3 Upgrade Spring Boot Plugin
- [ ] `org.springframework.boot` plugin: `2.x` → `3.2.x`
- [ ] `io.spring.dependency-management` plugin: `1.0.x` → `1.1.x`

### 1.4 Verify: Project resolves dependencies
```bash
./gradlew dependencies --no-daemon   # or mvn dependency:tree
```

---

## Phase 2: Jakarta Namespace Migration (`javax.*` → `jakarta.*`)

### 2.1 Identify all `javax.*` imports that need migration
```bash
grep -rn "import javax\." src/ --include="*.java" | grep -v "javax.crypto" | grep -v "javax.net" | grep -v "javax.security.auth"
```

### 2.2 Migrate these packages
| Old Package | New Package | Notes |
|---|---|---|
| `javax.servlet.*` | `jakarta.servlet.*` | Servlet API |
| `javax.validation.*` | `jakarta.validation.*` | Bean Validation |
| `javax.persistence.*` | `jakarta.persistence.*` | JPA (if used) |
| `javax.annotation.*` | `jakarta.annotation.*` | `@PostConstruct`, `@PreDestroy`, etc. |
| `javax.transaction.*` | `jakarta.transaction.*` | JTA |
| `javax.websocket.*` | `jakarta.websocket.*` | WebSocket |
| `javax.mail.*` | `jakarta.mail.*` | JavaMail |
| `javax.inject.*` | `jakarta.inject.*` | CDI |

### 2.3 Do NOT migrate these (JDK-internal)
- `javax.crypto.*`
- `javax.net.*`
- `javax.security.auth.*`
- `javax.sql.*`
- `javax.xml.*`
- `javax.management.*`

### 2.4 Verify: Compilation passes
```bash
./gradlew compileJava --no-daemon
```

---

## Phase 3: Spring Security (if used)

### 3.1 Remove `WebSecurityConfigurerAdapter`
- [ ] Replace `extends WebSecurityConfigurerAdapter` with standalone `@Configuration` class
- [ ] Replace `@Override configure(HttpSecurity)` with `@Bean SecurityFilterChain`
- [ ] Replace `@Override configure(AuthenticationManagerBuilder)` with `@Bean AuthenticationManager`

### 3.2 Update Security DSL
| Before (Spring Security 5.x) | After (Spring Security 6.x) |
|---|---|
| `.csrf().disable()` | `.csrf(csrf -> csrf.disable())` |
| `.cors().and()` | `.cors(cors -> cors.configurationSource(...))` |
| `.authorizeRequests()` | `.authorizeHttpRequests()` |
| `.antMatchers(...)` | `.requestMatchers(...)` |
| `.mvcMatchers(...)` | `.requestMatchers(...)` |
| `.regexMatchers(...)` | Removed — use `RegexRequestMatcher` |
| `.access("hasRole('X')")` | `.hasRole("X")` or `AuthorizationManager` |

### 3.3 Check `@AuthenticationPrincipal` behavior
- In Spring Security 6, `SecurityContextHolder` uses `ThreadLocal` by default (no change), but `REQUEST_ATTRIBUTE` mode is now available.

### 3.4 Verify: Security tests pass
```bash
./gradlew test --tests "*Security*" --no-daemon
```

---

## Phase 4: JPA / Hibernate (if used)

### 4.1 Hibernate 5.x → 6.x changes
- [ ] `javax.persistence.*` → `jakarta.persistence.*`
- [ ] `hibernate.dialect` is now auto-detected — remove explicit dialect setting unless needed
- [ ] `@Type(type = "...")` → `@JdbcTypeCode(...)` or `@Type(value = ...)` (Hibernate 6 annotation API)
- [ ] ID generation strategy changes: `GenerationType.AUTO` now defaults to `SEQUENCE` on most databases
- [ ] Check `ImplicitNamingStrategy` / `PhysicalNamingStrategy` — defaults may have changed

### 4.2 Flyway / Liquibase
- [ ] Ensure Flyway 9+ or Liquibase 4.17+ (Spring Boot 3.2 manages these)
- [ ] Verify migrations run cleanly against a fresh database

---

## Phase 5: Third-Party Library Upgrades

### 5.1 Common libraries requiring upgrades

| Library | Spring Boot 2.x Version | Spring Boot 3.x Version | Key Changes |
|---|---|---|---|
| MyBatis Spring Boot | 2.x | 3.0+ | Jakarta namespace |
| Netflix DGS | 4.x–5.x | 7.x–8.x+ | `onException` → `handleException`, PageInfo API |
| JJWT | 0.11.x | 0.12.x | `setSubject` → `subject`, `parserBuilder` → `parser`, key size enforcement |
| REST Assured | 4.x | 5.x | Jakarta namespace |
| Springfox (Swagger) | 3.x | **REMOVED** | Replace with `springdoc-openapi 2.x` |
| Mockito | mockito-inline 4.x | mockito-core 5.x | Inline mocking is now default |
| Querydsl | 5.0.x | 5.0.x+ | `javax.annotation.Generated` → `jakarta.annotation.Generated` |

### 5.2 Check for abandoned libraries
- If a dependency has no Jakarta-compatible release, find an alternative or fork.

### 5.3 Verify: Full compilation passes
```bash
./gradlew compileJava compileTestJava --no-daemon
```

---

## Phase 6: Configuration & Properties

### 6.1 Renamed/removed properties
| Old Property | New Property / Action |
|---|---|
| `spring.redis.*` | `spring.data.redis.*` |
| `spring.elasticsearch.*` | `spring.elasticsearch.uris` (restructured) |
| `spring.jpa.hibernate.use-new-id-generator-mappings` | Removed (always true) |
| `server.max-http-header-size` | `server.max-http-request-header-size` |
| `management.metrics.export.*` | `management.<product>.metrics.export.*` |
| `spring.mvc.throw-exception-if-no-handler-found` | Now `true` by default |

### 6.2 Actuator changes
- [ ] `/actuator/env` no longer shows `@ConfigurationProperties` by default
- [ ] `@Endpoint` annotations may need review
- [ ] Check actuator security configuration

### 6.3 `@ConstructorBinding`
- [ ] Moved from `org.springframework.boot.context.properties.ConstructorBinding` to `org.springframework.boot.context.properties.bind.ConstructorBinding`
- [ ] No longer needed on single-constructor classes (auto-detected)

---

## Phase 7: Test Suite

### 7.1 Run all tests
```bash
./gradlew test --no-daemon
```

### 7.2 Common test issues
- [ ] `@MockBean` / `@SpyBean` — Still work but are deprecated in 3.2+. Future migration to `@MockitoBean`.
- [ ] `TestRestTemplate` — Behavior changes with trailing slashes
- [ ] `WebMvcTest` — Auto-configuration changes may require additional `@Import`
- [ ] `@DataJpaTest` — May need `@AutoConfigureTestDatabase` adjustments
- [ ] Mockito version bump — `mockito-inline` merged into `mockito-core`

### 7.3 Fix and re-run until all tests pass
```bash
./gradlew test --no-daemon
# Repeat until: BUILD SUCCESSFUL
```

---

## Phase 8: Build & Package

### 8.1 Full build
```bash
# Skip known non-blocking checks if needed
./gradlew build -x spotlessJava -x spotlessJavaCheck -x jacocoTestCoverageVerification --no-daemon
```

### 8.2 Boot the application
```bash
./gradlew bootRun --no-daemon
# Verify startup logs show Spring Boot 3.x banner
# Test key endpoints manually
```

### 8.3 Docker image (if applicable)
```bash
./gradlew bootBuildImage --no-daemon
# Ensure base image uses Java 17+
```

---

## Phase 9: Documentation & PR

- [ ] Update `README.md` — Change Java version requirement (11 → 17)
- [ ] Create `UPGRADE_NOTES.md` — Document every breaking change with before/after code
- [ ] Commit and push
- [ ] Open PR with detailed description of all changes
- [ ] Link to Spring Boot 3.0 Migration Guide in PR description

---

## Post-Merge Verification

- [ ] CI/CD pipeline passes with Java 17
- [ ] Application starts and serves traffic in staging
- [ ] Smoke test critical user flows
- [ ] Monitor error rates for 24-48 hours after deployment
- [ ] Check for deprecation warnings in logs (`grep -i "deprecated"`)

---

## Quick Reference: Useful Commands

```bash
# Find all javax imports that need migration
grep -rn "import javax\." src/ --include="*.java" | grep -v "javax.crypto\|javax.net\|javax.security.auth\|javax.sql\|javax.xml"

# Find WebSecurityConfigurerAdapter usage
grep -rn "WebSecurityConfigurerAdapter" src/ --include="*.java"

# Find antMatchers usage
grep -rn "antMatchers\|mvcMatchers" src/ --include="*.java"

# Find deprecated Spring properties
grep -rn "spring.redis\.\|spring.elasticsearch\.\|use-new-id-generator" src/main/resources/

# Find @ConstructorBinding usage
grep -rn "@ConstructorBinding" src/ --include="*.java"
```

---

## Resources

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration](https://docs.spring.io/spring-security/reference/migration/index.html)
- [Jakarta EE 10 Release Notes](https://jakarta.ee/release/10/)
- [Hibernate 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc)
- [OpenRewrite Spring Boot 3 Recipes](https://docs.openrewrite.org/recipes/java/spring/boot3)
