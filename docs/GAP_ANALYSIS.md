# Gap Analysis — RealWorld "Conduit" Application

This document assesses the codebase against seven engineering best-practice categories. Each gap is rated by **Severity** (Critical / High / Medium / Low) and **Effort** to remediate (Small / Medium / Large).

---

## 1. Code Organization

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| CO-1 | **Outdated Spring Boot version** | Spring Boot 2.6.3 (released Jan 2022) is EOL. The project targets Java 11 but runs on Java 17. Upgrading to Spring Boot 3.x (Jakarta EE) would bring security patches, performance improvements, and long-term support. | Critical | Large |
| CO-2 | **Outdated frontend stack** | Next.js 9.5.1, React 16.13.1, and TypeScript 3.9.7 are multiple major versions behind. Next.js is now on v14+ with App Router and React Server Components. | Critical | Large |
| CO-3 | **javax → jakarta migration needed** | All `javax.validation` and `javax.servlet` imports must become `jakarta.*` for Spring Boot 3.x. Affects every controller, filter, and validation annotation. | High | Medium |
| CO-4 | **Deprecated `WebSecurityConfigurerAdapter`** | `WebSecurityConfig` extends the deprecated `WebSecurityConfigurerAdapter`. Spring Security 6+ requires the `SecurityFilterChain` bean approach. | High | Small |
| CO-5 | **No shared DTO/error library** | Exception classes, error resources, and validation patterns are only usable within this monolith. If split into microservices, these would need to be extracted into a shared module. | Medium | Medium |
| CO-6 | **Mixed REST + GraphQL in one deployment** | Both API styles share the same Spring Security filter chain, which complicates per-endpoint auth rules. No clear separation of concerns between the two API layers. | Medium | Medium |
| CO-7 | **Joda-Time instead of java.time** | Uses `org.joda.time.DateTime` throughout entities and DTOs. Joda-Time is in maintenance mode; `java.time` is the standard since Java 8. | Medium | Medium |
| CO-8 | **Hardcoded frontend API URL** | `SERVER_BASE_URL = http://localhost:8080` is hardcoded in `frontend/lib/utils/constant.ts`. Should be environment-configurable. | Medium | Small |
| CO-9 | **LoginParam and NewCommentParam in controller files** | Inner/package-private DTOs defined in the same file as controllers (`UsersApi.java`, `CommentsApi.java`) instead of separate files. | Low | Small |

---

## 2. Error Handling

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| EH-1 | **Inconsistent HTTP status codes** | `InvalidAuthenticationException` returns 422 (UNPROCESSABLE_ENTITY) instead of 401. Login failures should return 401 Unauthorized, not 422. | High | Small |
| EH-2 | **No global catch-all handler** | `CustomizeExceptionHandler` only handles `InvalidRequestException`, `InvalidAuthenticationException`, `MethodArgumentNotValidException`, and `ConstraintViolationException`. Unhandled exceptions bubble up as generic 500 with stack traces. | High | Small |
| EH-3 | **Silent exception swallowing in JWT** | `DefaultJwtService.getSubFromToken()` catches all `Exception` and returns `Optional.empty()` — expired tokens, malformed tokens, and signature failures are all silently ignored. No logging. | Medium | Small |
| EH-4 | **Inconsistent error response format** | REST errors use `ErrorResource` with `fieldErrors`, while GraphQL uses DGS `TypedGraphQLError` with different extensions. Frontend must handle two different error shapes. | Medium | Small |
| EH-5 | **No error message in `NoAuthorizationException`** | Thrown with no message, making it hard to debug which resource was denied. | Low | Small |
| EH-6 | **Duplicate error handling logic** | `GraphQLCustomizeExceptionHandler` duplicates the `getParam()` method and `ConstraintViolation` processing logic from `CustomizeExceptionHandler`. | Low | Small |

---

## 3. Testing

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| T-1 | **JaCoCo coverage at ~33% (threshold 80%)** | The 80% threshold is configured but must be skipped (`-x jacocoTestCoverageVerification`) because actual coverage is only about 33%. | High | Large |
| T-2 | **No GraphQL API tests** | The entire DGS GraphQL layer (7 data fetcher/mutation classes) has zero test coverage. | High | Medium |
| T-3 | **No frontend tests** | The Next.js frontend has no unit tests, no integration tests, and no E2E test framework configured. | High | Large |
| T-4 | **No contract tests** | No Pact or Spring Cloud Contract tests to validate API contracts between frontend and backend. | Medium | Medium |
| T-5 | **Selenium E2E suite is minimal** | Only `SeleniumSetupTest` exists — a browser connectivity smoke test, not a functional test suite. | Medium | Medium |
| T-6 | **All API tests are mock-based** | Controller tests mock all dependencies via `@MockBean`. No integration tests that exercise the actual DB layer through controllers. | Medium | Medium |
| T-7 | **No test for seed data** | Flyway V2 seed data could break silently with schema changes — no test validates it. | Low | Small |

---

## 4. Security

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| S-1 | **JWT secret hardcoded in `application.properties`** | `jwt.secret=nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA` is committed to source control. Must be externalized. | Critical | Small |
| S-2 | **No CSRF protection** | CSRF is explicitly disabled (`http.csrf().disable()`). While this is common for stateless JWT APIs, the rationale is not documented and there is no alternative mitigation. | Medium | Small |
| S-3 | **CORS allows all origins** | `configuration.setAllowedOrigins(asList("*"))` — any domain can make requests to the API. Should be restricted in production. | High | Small |
| S-4 | **No rate limiting** | No rate limiting on login endpoint (`/users/login`) or registration. Susceptible to brute-force and account enumeration attacks. | High | Medium |
| S-5 | **Passwords stored without validation** | No password strength requirements — the `RegisterParam` only validates `@NotBlank`. Users can register with single-character passwords. | Medium | Small |
| S-6 | **No foreign key constraints in DB** | SQLite schema uses implicit FK references but no `FOREIGN KEY` constraints or `ON DELETE CASCADE`. Orphaned records are possible. | Medium | Small |
| S-7 | **Deprecated JJWT `SignatureAlgorithm` usage** | Uses `SignatureAlgorithm.HS512` enum which is deprecated in newer jjwt versions. | Low | Small |
| S-8 | **No input sanitization for article body** | Article body (`text`) is stored and returned as-is. Potential XSS vector if rendered without escaping. | Medium | Small |
| S-9 | **Default BCrypt rounds** | Uses default BCrypt rounds (10). While adequate, not configurable for future strengthening. | Low | Small |
| S-10 | **No dependency vulnerability scanning** | No OWASP Dependency-Check, Snyk, or similar tool configured in the build. | Medium | Small |

---

## 5. API Design

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| AD-1 | **No API versioning** | All endpoints are unversioned (e.g., `/articles` not `/v1/articles`). Breaking changes would affect all clients simultaneously. | High | Medium |
| AD-2 | **No OpenAPI/Swagger documentation** | No `springdoc-openapi` or Swagger dependency. No machine-readable API spec is generated. | High | Small |
| AD-3 | **Inconsistent response wrapping** | Responses are manually wrapped in `HashMap<String, Object>` with string keys like `"article"`, `"user"`, `"profile"`. No typed response wrapper classes. | Medium | Medium |
| AD-4 | **`ResponseEntity<?>` wildcard return types** | Most controller methods return `ResponseEntity<?>` or raw `ResponseEntity`, losing compile-time type safety and making OpenAPI generation inaccurate. | Medium | Small |
| AD-5 | **No HATEOAS links despite dependency** | `spring-boot-starter-hateoas` is in dependencies but never used. No hypermedia links in responses. | Low | Small |
| AD-6 | **Create article returns 200 instead of 201** | `ArticlesApi.createArticle()` returns `ResponseEntity.ok()` (200) instead of `ResponseEntity.status(201)`. | Medium | Small |
| AD-7 | **No content negotiation** | API only supports JSON. No `Accept` header handling or content type negotiation. | Low | Small |
| AD-8 | **Pagination metadata inconsistency** | REST returns `{ articles: [...], articlesCount: N }` while GraphQL returns Relay-style `{ edges: [...], pageInfo: {...} }`. No standard pagination envelope. | Medium | Small |

---

## 6. Observability

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| O-1 | **No structured logging** | Uses default Spring Boot logging with no structured format (JSON), no correlation IDs, and no request tracing. | High | Medium |
| O-2 | **No health check endpoint** | Spring Boot Actuator is not included. No `/actuator/health`, `/actuator/info`, or readiness/liveness probes. | High | Small |
| O-3 | **No metrics collection** | No Micrometer, Prometheus, or any metrics framework. Cannot measure request rates, latencies, or error rates. | High | Medium |
| O-4 | **No distributed tracing** | No Sleuth/Zipkin, OpenTelemetry, or any tracing framework. Cannot trace requests across REST and GraphQL layers. | Medium | Medium |
| O-5 | **Debug-level logging in production config** | `application.properties` sets `logging.level.io.spring.infrastructure.mybatis=DEBUG` which would flood production logs. | Medium | Small |
| O-6 | **No request/response logging** | No HTTP request logging filter for debugging API issues. | Low | Small |

---

## 7. Resilience

| # | Gap | Description | Severity | Effort |
|---|-----|-------------|----------|--------|
| R-1 | **No circuit breaker pattern** | If the SQLite database file becomes locked or corrupted, all requests fail with no fallback. | Medium | Medium |
| R-2 | **No request timeouts** | No explicit HTTP request timeouts configured. `mybatis.configuration.default-statement-timeout=3000` (3000 seconds!) is likely a misconfiguration. | High | Small |
| R-3 | **No retry logic** | No retry mechanism for transient DB errors (e.g., SQLite busy/locked). | Medium | Small |
| R-4 | **No graceful shutdown** | No `server.shutdown=graceful` configuration. In-flight requests may be terminated abruptly. | Medium | Small |
| R-5 | **SQLite not suitable for production** | SQLite lacks concurrent write support, connection pooling, and replication. Only suitable for development/testing. | Critical | Large |
| R-6 | **DB recreated on every boot** | `bootRun` deletes and recreates `dev.db` on startup. While useful for dev, this behavior has no production/dev profile separation. | High | Small |
| R-7 | **No idempotency controls** | Favorite/unfavorite and follow/unfollow operations have no idempotency protection — duplicate `POST` calls may create duplicate records or throw errors. | Medium | Small |
| R-8 | **No connection pool configuration** | No HikariCP tuning. Default pool settings may not be appropriate for production workloads. | Low | Small |

---

## Summary Table

| Category | Critical | High | Medium | Low | Total |
|----------|----------|------|--------|-----|-------|
| Code Organization | 2 | 2 | 4 | 1 | 9 |
| Error Handling | 0 | 2 | 2 | 2 | 6 |
| Testing | 0 | 3 | 3 | 1 | 7 |
| Security | 1 | 2 | 4 | 2 | 9 (+1 Critical) |
| API Design | 0 | 2 | 3 | 2 | 7 (+1 High) |
| Observability | 0 | 3 | 2 | 1 | 6 |
| Resilience | 1 | 2 | 3 | 1 | 7 (+1 Critical) |
| **Totals** | **4** | **16** | **21** | **10** | **51** |

### Critical Gaps (Require Immediate Attention)
1. **CO-1**: Outdated Spring Boot 2.6.3 (EOL)
2. **CO-2**: Outdated frontend stack (Next.js 9, React 16)
3. **S-1**: JWT secret hardcoded in source control
4. **R-5**: SQLite not suitable for production use
