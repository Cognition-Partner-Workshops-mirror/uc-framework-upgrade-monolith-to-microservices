# Article Microservice Extraction Decisions

## Overview

This document explains the domain boundary choices made when extracting the "Article" bounded context from the RealWorld blogging platform monolith into a standalone microservice.

---

## 1. Domain Boundary Analysis

### 1.1 Bounded Context Identification

The RealWorld platform has two natural bounded contexts:

| Context | Entities | Responsibilities |
|---------|----------|-----------------|
| **User** | User, FollowRelation | Registration, authentication, profiles, following |
| **Article** | Article, Tag, Comment, ArticleFavorite | Content creation, tagging, commenting, favoriting |

### 1.2 Why "Article" Was Chosen for Extraction

1. **Clear domain boundary**: Articles, tags, comments, and favorites form a cohesive aggregate with minimal cross-context dependencies.
2. **Single foreign key**: The only cross-context reference is `userId` in Article, Comment, and ArticleFavorite — a simple string ID, not a JPA relationship.
3. **Independent lifecycle**: Articles can be created, updated, and deleted without affecting user data.
4. **Query patterns**: Most article queries (list, filter by tag, search) don't require user data beyond profile display.
5. **Write independence**: Article writes (create, update, delete, comment, favorite) only need to validate the user exists, not modify user data.

### 1.3 What Stays in the Monolith (User Service)

| Component | Reason for Staying |
|----------|-------------------|
| `User` entity | Core identity/auth aggregate |
| `FollowRelation` | Belongs to user-to-user relationships |
| `UserRepository` | Owns user data |
| `ProfileQueryService` | Profiles are about users, not articles |
| JWT authentication | Centralized auth concern |
| Spring Security config | Stays with the auth owner |

---

## 2. Cross-Service Communication

### 2.1 Communication Pattern

**Synchronous REST** was chosen over async messaging for the initial extraction:

| Pattern | Pros | Cons | Decision |
|---------|------|------|----------|
| REST (chosen) | Simple, familiar, request-response | Coupling, latency | Good for first extraction |
| Message Queue | Decoupled, resilient | Complexity, eventual consistency | Future evolution |
| gRPC | Fast, typed contracts | Learning curve, tooling | Future optimization |

### 2.2 API Contracts

#### Article Service → User Service

| Endpoint | Method | Purpose | Request | Response |
|----------|--------|---------|---------|----------|
| `/profiles/{username}` | GET | Resolve author profile | — | `{ "profile": { "username", "bio", "image", "following" } }` |
| `/user` | GET | Validate auth token | `Authorization: Token <jwt>` | `{ "user": { "id", "username", ... } }` |

#### User Service → Article Service

| Endpoint | Method | Purpose | Request | Response |
|----------|--------|---------|---------|----------|
| `/articles` | GET | List articles | Query params: `offset`, `limit`, `tag`, `author`, `favorited` | `{ "articles": [...], "articlesCount": N }` |
| `/articles` | POST | Create article | `{ "article": { "title", "description", "body", "tagList" } }` | `{ "article": { ... } }` |
| `/articles/{slug}` | GET | Get single article | — | `{ "article": { ... } }` |
| `/articles/{slug}` | PUT | Update article | `{ "article": { "title", "description", "body" } }` | `{ "article": { ... } }` |
| `/articles/{slug}` | DELETE | Delete article | — | 204 No Content |
| `/articles/{slug}/comments` | GET/POST | Comments CRUD | — | `{ "comments": [...] }` or `{ "comment": { ... } }` |
| `/articles/{slug}/favorite` | POST/DELETE | Favorite/unfavorite | — | `{ "article": { ..., "favorited", "favoritesCount" } }` |
| `/health` | GET | Health check | — | `{ "status": "UP", "service": "article-service" }` |
| `/actuator/health` | GET | Actuator health | — | `{ "status": "UP" }` |

### 2.3 Authentication Strategy

**Shared JWT secret** approach was chosen for the initial extraction:

- Both services share the same JWT signing secret (`jwt.secret` in application.properties).
- Article-service can independently validate JWT tokens without calling user-service.
- This avoids an auth round-trip on every request.
- **Future evolution**: Migrate to an external auth service (e.g., Keycloak) or OAuth2 resource server pattern.

---

## 3. Data Ownership

### 3.1 Database Strategy

**Separate databases per service** (Database per Service pattern):

| Service | Database | Rationale |
|---------|----------|-----------|
| User Service | `dev.db` (SQLite) | Owns user, follow tables |
| Article Service | `article-dev.db` (SQLite) | Owns article, tag, comment, favorite tables |

### 3.2 Data Migration Approach

For the initial extraction, article-service uses in-memory storage (ConcurrentHashMap) to demonstrate the service architecture without requiring a data migration. In production:

1. Extract article-related tables from the monolith SQLite database.
2. Set up Flyway migrations in article-service.
3. Run a one-time data migration script.
4. Switch traffic to article-service endpoints.

---

## 4. Extracted Components

### 4.1 Files Extracted to article-service

| Category | Monolith Package | Article-Service Package |
|----------|-----------------|----------------------|
| Domain: Article | `io.spring.core.article` | `io.spring.articleservice.core.article` |
| Domain: Tag | `io.spring.core.article` | `io.spring.articleservice.core.article` |
| Domain: Comment | `io.spring.core.comment` | `io.spring.articleservice.core.comment` |
| Domain: Favorite | `io.spring.core.favorite` | `io.spring.articleservice.core.favorite` |
| API: Articles | `io.spring.api.ArticlesApi` | `io.spring.articleservice.api.ArticlesController` |
| API: Comments | `io.spring.api.CommentsApi` | `io.spring.articleservice.api.CommentsController` |
| API: Favorites | `io.spring.api.ArticleFavoriteApi` | `io.spring.articleservice.api.FavoritesController` |

### 4.2 New Components (Not in Monolith)

| Component | Purpose |
|----------|---------|
| `UserServiceClient` | REST client for cross-service calls to user-service |
| `RestTemplateConfig` | RestTemplate bean with timeouts for resilient communication |
| `HealthController` | Custom health endpoint for service monitoring |
| `ArticleDTO` | Data transfer object for article data across service boundary |
| `CommentDTO` | Data transfer object for comment data |
| `ProfileDTO` | Data transfer object for user profiles received from user-service |
| `NewArticleDTO` | Inbound DTO for article creation requests |
| `NewCommentDTO` | Inbound DTO for comment creation requests |

---

## 5. Infrastructure Decisions

### 5.1 Docker Compose

Both services run via Docker Compose with:
- **Separate builds**: Each service has its own Dockerfile with multi-stage build.
- **Health checks**: Both services expose `/actuator/health` for container orchestration.
- **Dependency ordering**: Article-service depends on user-service being healthy before starting.
- **Networking**: Both services share a `realworld-net` bridge network for inter-service communication.
- **Volumes**: Separate named volumes for each service's SQLite database.

### 5.2 Port Allocation

| Service | Port | Purpose |
|---------|------|---------|
| User Service (monolith) | 8080 | Existing API port, backward compatible |
| Article Service | 8081 | New dedicated port for article operations |

---

## 6. Graceful Degradation

The `UserServiceClient` implements fallback behavior:
- If user-service is unavailable, profile lookups return `Optional.empty()`.
- Article operations continue to work even if profile resolution fails.
- Timeouts are set at 5 seconds for both connect and read to prevent cascading failures.

---

## 7. Future Improvements

1. **API Gateway**: Add an API gateway (e.g., Spring Cloud Gateway) to route requests to the appropriate service.
2. **Service Discovery**: Add Consul or Eureka for dynamic service discovery instead of hardcoded URLs.
3. **Circuit Breaker**: Add Resilience4j circuit breaker to `UserServiceClient` for better fault tolerance.
4. **Event-Driven**: Replace synchronous REST calls with async events (e.g., user profile updated → article-service cache invalidation).
5. **Shared Library**: Extract common DTOs and utilities into a shared library module.
6. **Database Migration**: Add Flyway migrations to article-service for proper persistent storage.
7. **Contract Testing**: Add Pact or Spring Cloud Contract tests for the API contracts between services.
