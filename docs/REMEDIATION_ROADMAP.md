# Remediation Roadmap — RealWorld "Conduit" Application

Gaps from `GAP_ANALYSIS.md` are prioritized into three phases:
- **Phase 1 — Quick Wins**: High-severity / low-effort items that reduce immediate risk
- **Phase 2 — Important**: High-severity / medium-effort items that improve reliability and maintainability
- **Phase 3 — Polish**: Lower-severity items that improve developer experience and code quality

Each item includes an actionable **Devin prompt** that can be executed directly.

---

## Phase 1 — Quick Wins (1–2 weeks)

### 1.1 Externalize JWT Secret (S-1)
**Gap**: JWT secret is hardcoded in `application.properties` and committed to source control.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, externalize the JWT secret. Replace the hardcoded `jwt.secret` value in `src/main/resources/application.properties` with a placeholder `${JWT_SECRET}` and add a fallback for local dev using a Spring profile. Add `jwt.secret` to `.gitignore` patterns if it appears in any `.env` file. Update `README.md` to document the required environment variable. Ensure all existing tests still pass by setting the secret in `application-test.properties`.

---

### 1.2 Add Health Check / Actuator Endpoint (O-2)
**Gap**: No Spring Boot Actuator — no health, info, or readiness endpoints.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add Spring Boot Actuator. Add `implementation 'org.springframework.boot:spring-boot-starter-actuator'` to `build.gradle`. Configure `management.endpoints.web.exposure.include=health,info,metrics` in `application.properties`. Add the actuator endpoints to the Spring Security permit list in `WebSecurityConfig.java`. Verify `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`. Run the test suite to ensure nothing breaks.

---

### 1.3 Fix Authentication Error HTTP Status (EH-1)
**Gap**: `InvalidAuthenticationException` returns 422 instead of 401.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, fix the HTTP status for authentication failures. In `CustomizeExceptionHandler.java`, change the `handleInvalidAuthentication` method to return `HttpStatus.UNAUTHORIZED` (401) instead of `UNPROCESSABLE_ENTITY` (422). Update the corresponding test in `UsersApiTest.java` if it asserts 422 for login failure. Run `./gradlew test -x jacocoTestCoverageVerification --no-daemon` to confirm all tests pass.

---

### 1.4 Add Global Catch-All Exception Handler (EH-2)
**Gap**: Unhandled exceptions return 500 with stack traces.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add a catch-all `@ExceptionHandler(Exception.class)` to `CustomizeExceptionHandler.java` that returns a generic 500 response with `{"errors": {"body": ["internal server error"]}}` and logs the full stack trace at ERROR level. Ensure the handler does not expose internal details in the response body. Run the test suite to verify.

---

### 1.5 Restrict CORS Origins (S-3)
**Gap**: CORS allows all origins (`*`).

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, restrict CORS origins. In `WebSecurityConfig.java`, change `configuration.setAllowedOrigins(asList("*"))` to use a configurable property `cors.allowed-origins` with a default of `http://localhost:3000`. Add the property to `application.properties`. Document the CORS configuration in the README. Run tests to verify.

---

### 1.6 Fix MyBatis Statement Timeout (R-2)
**Gap**: `mybatis.configuration.default-statement-timeout=3000` is 3000 seconds (50 minutes).

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, fix the MyBatis statement timeout. Change `mybatis.configuration.default-statement-timeout=3000` to `mybatis.configuration.default-statement-timeout=30` (30 seconds) in `application.properties`. Run the test suite to ensure no tests time out with the new value.

---

### 1.7 Add OpenAPI Documentation (AD-2)
**Gap**: No machine-readable API specification.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add OpenAPI documentation. Add `implementation 'org.springdoc:springdoc-openapi-ui:1.6.15'` to `build.gradle`. Verify that Swagger UI is accessible at `http://localhost:8080/swagger-ui.html` and that `/v3/api-docs` returns a valid OpenAPI 3.0 spec. Add the Swagger UI URL to the Spring Security permit list. Update the README with the Swagger URL.

---

### 1.8 Fix Article Creation HTTP Status (AD-6)
**Gap**: `POST /articles` returns 200 instead of 201.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, fix the HTTP status code for article creation. In `ArticlesApi.java`, change `ResponseEntity.ok(...)` to `ResponseEntity.status(HttpStatus.CREATED).body(...)` in the `createArticle` method. Update the corresponding test in `ArticlesApiTest.java` if it asserts status 200 for article creation. Run the test suite.

---

### 1.9 Remove Debug Logging from Production Config (O-5)
**Gap**: DEBUG-level MyBatis logging would flood production logs.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, move debug logging to a dev profile. Remove the two `logging.level.io.spring.infrastructure.mybatis=DEBUG` lines from `application.properties`. Create `application-dev.properties` with those debug logging settings. Update `README.md` to document the dev profile: `./gradlew bootRun --args='--spring.profiles.active=dev'`.

---

### 1.10 Add Graceful Shutdown (R-4)
**Gap**: No graceful shutdown configuration.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, enable graceful shutdown. Add `server.shutdown=graceful` and `spring.lifecycle.timeout-per-shutdown-phase=30s` to `application.properties`. Verify the app shuts down cleanly by running `./gradlew bootRun --no-daemon`, then sending a SIGTERM.

---

### 1.11 Add Password Strength Validation (S-5)
**Gap**: No password strength requirements for registration.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add password validation. In `RegisterParam.java`, add `@Size(min = 8, max = 100, message = "must be between 8 and 100 characters")` to the `password` field. Update `UsersApiTest.java` to add a test case verifying that short passwords are rejected with 422. Run the test suite.

---

### 1.12 Add Dependency Vulnerability Scanning (S-10)
**Gap**: No OWASP Dependency-Check or similar tool in the build.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add OWASP Dependency-Check. Add `id 'org.owasp.dependencycheck' version '9.0.9'` to the `plugins` block in `build.gradle`. Configure it to fail the build on CVSS score >= 7. Run `./gradlew dependencyCheckAnalyze --no-daemon` and document any findings.

---

## Phase 2 — Important (2–6 weeks)

### 2.1 Upgrade Spring Boot to 3.x (CO-1, CO-3, CO-4)
**Gap**: Spring Boot 2.6.3 is EOL. Requires javax→jakarta migration and security config modernization.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, upgrade Spring Boot from 2.6.3 to 3.2.x. This involves: (1) Update `build.gradle` to use `org.springframework.boot` version `3.2.5` and `io.spring.dependency-management` version `1.1.4`. (2) Replace all `javax.validation` imports with `jakarta.validation`, all `javax.servlet` with `jakarta.servlet`, and `javax.crypto` with `jakarta` equivalents where needed. (3) Replace `WebSecurityConfigurerAdapter` in `WebSecurityConfig.java` with a `@Bean SecurityFilterChain` method. (4) Update the DGS framework dependency to a version compatible with Spring Boot 3.x (e.g., `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:7.x`). (5) Set `sourceCompatibility` and `targetCompatibility` to `17`. (6) Run the full test suite and fix any compilation or test failures.

---

### 2.2 Add Structured Logging (O-1)
**Gap**: No structured logging, no correlation IDs.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add structured JSON logging. Add `implementation 'net.logstash.logback:logstash-logback-encoder:7.4'` to `build.gradle`. Create `src/main/resources/logback-spring.xml` with a JSON console appender for the `production` profile and a plain text appender for `default`. Add an MDC filter that generates a `requestId` (UUID) for each HTTP request and includes it in all log output. Ensure the existing tests still pass.

---

### 2.3 Add Metrics with Micrometer (O-3)
**Gap**: No metrics collection for monitoring.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add Micrometer metrics. Add `implementation 'io.micrometer:micrometer-registry-prometheus'` to `build.gradle` (Actuator is a prerequisite — see Phase 1.2). Expose the Prometheus endpoint at `/actuator/prometheus`. Add custom metrics: (1) A counter for user registrations, (2) A counter for article creations, (3) A timer around `ArticleQueryService.findRecentArticles`. Document the metrics endpoint in the README.

---

### 2.4 Add GraphQL Tests (T-2)
**Gap**: Zero test coverage for the GraphQL layer.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add tests for the GraphQL API. Create test classes for `ArticleMutation`, `UserMutation`, `CommentMutation`, and `RelationMutation` using DGS's `DgsQueryExecutor` test support. Cover: (1) `createUser` mutation with valid and invalid input, (2) `login` mutation with correct and incorrect credentials, (3) `createArticle` and `articles` query, (4) `addComment` and comment retrieval. Use the existing `TestWithCurrentUser` pattern for authentication. Run the test suite and verify all new tests pass.

---

### 2.5 Add Rate Limiting (S-4)
**Gap**: No rate limiting on login or registration endpoints.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add rate limiting. Add `implementation 'com.bucket4j:bucket4j-core:8.7.0'` to `build.gradle`. Create a `RateLimitFilter` (OncePerRequestFilter) that limits `/users/login` to 5 requests per minute per IP and `/users` (POST) to 3 requests per minute per IP. Return 429 Too Many Requests with a `Retry-After` header when the limit is exceeded. Add tests for the rate limiter. Run the full test suite.

---

### 2.6 Add Foreign Key Constraints (S-6)
**Gap**: SQLite schema has no FK constraints; orphaned records possible.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add foreign key constraints. Create a new Flyway migration `V3__add_foreign_keys.sql` that enables SQLite foreign keys (`PRAGMA foreign_keys = ON`) and recreates tables with proper `FOREIGN KEY` and `ON DELETE CASCADE` constraints for: `articles.user_id → users.id`, `comments.article_id → articles.id`, `comments.user_id → users.id`, `article_favorites.article_id → articles.id`, `article_favorites.user_id → users.id`, `article_tags.article_id → articles.id`, `article_tags.tag_id → tags.id`, `follows.user_id → users.id`, `follows.follow_id → users.id`. Migrate existing data. Run the test suite to verify no tests break.

---

### 2.7 Increase Test Coverage to 50%+ (T-1)
**Gap**: JaCoCo coverage is ~33%, target is 80%.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, improve test coverage. Add integration tests that exercise the full request path (controller → service → MyBatis → SQLite): (1) Test article CRUD lifecycle via MockMvc with a real database, (2) Test user registration and login flow, (3) Test follow/unfollow cycle, (4) Test favorite/unfavorite cycle. Use `@SpringBootTest` with the test profile. Aim for at least 50% line coverage. Update the JaCoCo minimum threshold in `build.gradle` from 0.80 to 0.50 as an interim target. Run `./gradlew test jacocoTestReport -x jacocoTestCoverageVerification --no-daemon` and report the new coverage percentage.

---

### 2.8 Add API Versioning (AD-1)
**Gap**: No API versioning strategy.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add URL-based API versioning. Create a base path `/api/v1` and move all existing REST endpoints under it (e.g., `/articles` → `/api/v1/articles`). Use a `@RequestMapping("/api/v1")` annotation on a new base controller class or configure the `server.servlet.context-path` property. Add redirect/forwarding from the old paths for backward compatibility. Update the frontend `SERVER_BASE_URL` constant. Update all tests. Run the full test suite.

---

### 2.9 Make Frontend API URL Configurable (CO-8)
**Gap**: `SERVER_BASE_URL` is hardcoded to `http://localhost:8080`.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, make the frontend API URL configurable. In `frontend/lib/utils/constant.ts`, change `SERVER_BASE_URL` to read from `process.env.NEXT_PUBLIC_API_URL` with a fallback to `http://localhost:8080`. Create a `.env.local.example` file documenting the variable. Update `README.md` with instructions for configuring the API URL. Verify the dev server still works.

---

### 2.10 Replace Joda-Time with java.time (CO-7)
**Gap**: Joda-Time is in maintenance mode; java.time is the standard.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, migrate from Joda-Time to java.time. Replace all `org.joda.time.DateTime` usage with `java.time.OffsetDateTime` or `java.time.Instant`. Update: (1) Domain entities `Article.java` and `Comment.java`, (2) DTOs `ArticleData.java` and `CommentData.java`, (3) `JacksonCustomizations.java` DateTime serializer, (4) `DateTimeHandler.java` MyBatis TypeHandler, (5) `DateTimeCursor.java` and `CursorPageParameter.java`, (6) All test files that construct DateTime objects. Remove the `joda-time:joda-time` dependency from `build.gradle`. Run the full test suite.

---

## Phase 3 — Polish (6+ weeks)

### 3.1 Upgrade Frontend to Next.js 14+ (CO-2)
**Gap**: Next.js 9.5.1 and React 16 are severely outdated.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, upgrade the frontend. Upgrade Next.js from 9.5.1 to 14.x, React from 16 to 18.x, and TypeScript from 3.9 to 5.x. Migrate pages from `pages/` router to App Router if feasible, or keep `pages/` with the new Next.js version. Update all deprecated APIs. Replace `swr` 0.3 with latest. Ensure the frontend builds (`npm run build`) and the dev server starts (`npm run dev`). Fix any TypeScript errors.

---

### 3.2 Add Frontend Tests (T-3)
**Gap**: No frontend tests of any kind.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add frontend tests. Set up Jest and React Testing Library in the `frontend/` directory. Add unit tests for: (1) `LoginForm` and `RegisterForm` components, (2) API utility functions in `lib/api/`, (3) `calculatePagination` utility, (4) `editorReducer`. Add a Playwright E2E test for the login flow. Configure test scripts in `package.json`. Aim for at least 30% coverage on frontend code.

---

### 3.3 Add Contract Tests (T-4)
**Gap**: No contract tests between frontend and backend.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add contract tests. Add Spring Cloud Contract to the backend: (1) Add `testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'` to `build.gradle`. (2) Create contract definitions for the key endpoints: `POST /users/login`, `GET /articles`, `POST /articles`, `GET /articles/{slug}`. (3) Generate and run contract tests. (4) Optionally create a stub JAR that the frontend can use for testing.

---

### 3.4 Add Distributed Tracing (O-4)
**Gap**: No tracing across REST and GraphQL layers.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add distributed tracing. Add `implementation 'io.micrometer:micrometer-tracing-bridge-otel'` and `implementation 'io.opentelemetry:opentelemetry-exporter-zipkin'` to `build.gradle`. Configure a Zipkin exporter pointing to `http://localhost:9411` (or configurable via property). Ensure trace IDs propagate through both REST and GraphQL requests. Add trace ID to the structured logging MDC context.

---

### 3.5 Migrate from SQLite to PostgreSQL (R-5)
**Gap**: SQLite is not suitable for production use.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add PostgreSQL support. (1) Add `runtimeOnly 'org.postgresql:postgresql'` to `build.gradle`. (2) Create `application-postgres.properties` with PostgreSQL connection settings. (3) Update Flyway migrations to use PostgreSQL-compatible SQL (or create a separate migration path). (4) Create a `docker-compose.yml` with a PostgreSQL service for local development. (5) Keep SQLite as the default for quick local dev. (6) Document the PostgreSQL setup in README.

---

### 3.6 Separate REST and GraphQL API Modules (CO-6)
**Gap**: Both API layers share the same security configuration.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, separate REST and GraphQL configurations. Create distinct `SecurityFilterChain` beans for REST (`/api/**`) and GraphQL (`/graphql`). Move GraphQL-specific exception handling to its own configuration. Consider moving the GraphQL layer into a separate Gradle subproject for cleaner separation. Ensure both APIs continue to work with existing authentication. Run all tests.

---

### 3.7 Add Typed Response Wrappers (AD-3, AD-4)
**Gap**: Responses use `HashMap<String, Object>` and `ResponseEntity<?>`.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, replace all `HashMap<String, Object>` response wrappers with typed record classes. Create response wrapper records: `ArticleResponse`, `ArticleListResponse`, `UserResponse`, `ProfileResponse`, `CommentResponse`, `CommentListResponse`, `TagsResponse`. Update all controller methods to return `ResponseEntity<SpecificType>` instead of `ResponseEntity<?>`. Ensure all tests pass and that the JSON output structure remains unchanged (same field names).

---

### 3.8 Remove Unused HATEOAS Dependency (AD-5)
**Gap**: `spring-boot-starter-hateoas` is included but never used.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, remove the unused HATEOAS dependency. Remove `implementation 'org.springframework.boot:spring-boot-starter-hateoas'` from `build.gradle`. Run a full build to ensure nothing depends on it. Run the test suite.

---

### 3.9 Add Idempotency Controls (R-7)
**Gap**: Duplicate POST calls for favorite/follow may cause errors.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add idempotency to favorite and follow operations. In `ArticleFavoriteApi.favoriteArticle()`, check if the favorite already exists before saving (return existing data if so). In `ProfileApi.follow()`, check if the follow relation already exists. Apply the same pattern to the GraphQL mutations. Add tests for duplicate calls. Run the full test suite.

---

### 3.10 Add CI/CD Pipeline
**Gap**: No CI/CD pipeline configured.

**Devin Prompt**:
> In the repo `uc-framework-upgrade-monolith-to-microservices`, add a GitHub Actions CI pipeline. Create `.github/workflows/ci.yml` that: (1) Runs on push to main and on PRs, (2) Sets up Java 17 and Node 16, (3) Runs `./gradlew build -x spotlessJava -x spotlessJavaCheck -x jacocoTestCoverageVerification --no-daemon`, (4) Runs `./gradlew test -x jacocoTestCoverageVerification --no-daemon`, (5) Runs `cd frontend && npm install && npm run build`, (6) Uploads JaCoCo test report as an artifact. Add a CI status badge to `README.md`.

---

## Priority Matrix

```
                    ┌─────────────────────────────────────────┐
                    │              EFFORT                      │
                    │   Small        Medium         Large      │
        ┌───────────┼─────────────┬──────────────┬────────────┤
        │ Critical  │ S-1 (1.1)   │              │ CO-1 (2.1) │
        │           │             │              │ CO-2 (3.1) │
   S    │           │             │              │ R-5  (3.5) │
   E    ├───────────┼─────────────┼──────────────┼────────────┤
   V    │ High      │ EH-1 (1.3)  │ S-4  (2.5)  │ T-1  (2.7) │
   E    │           │ EH-2 (1.4)  │ T-2  (2.4)  │ T-3  (3.2) │
   R    │           │ S-3  (1.5)  │ AD-1 (2.8)  │            │
   I    │           │ O-2  (1.2)  │              │            │
   T    │           │ AD-2 (1.7)  │              │            │
   Y    │           │ R-2  (1.6)  │              │            │
        │           │ R-6  (1.9)  │              │            │
        ├───────────┼─────────────┼──────────────┼────────────┤
        │ Medium    │ S-5  (1.11) │ CO-7 (2.10) │            │
        │           │ AD-6 (1.8)  │ O-1  (2.2)  │            │
        │           │ O-5  (1.9)  │ O-3  (2.3)  │            │
        │           │ R-4  (1.10) │ O-4  (3.4)  │            │
        │           │ S-8, S-10   │ T-4  (3.3)  │            │
        ├───────────┼─────────────┼──────────────┼────────────┤
        │ Low       │ CO-9        │              │            │
        │           │ AD-5 (3.8)  │              │            │
        └───────────┴─────────────┴──────────────┴────────────┘
```
