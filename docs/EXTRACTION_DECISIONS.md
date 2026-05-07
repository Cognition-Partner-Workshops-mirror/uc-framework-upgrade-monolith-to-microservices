# Article Service Extraction Decisions

## Overview

This document explains the domain boundary choices and architectural decisions made when extracting the "Article" bounded context from the RealWorld monolith into a standalone microservice.

---

## 1. Bounded Context Identification

### What was extracted

The **Article bounded context** encompasses all functionality related to content creation and consumption:

| Entity | Responsibility |
|---|---|
| `Article` | Core content entity (title, slug, body, description, timestamps) |
| `Tag` | Categorization labels attached to articles |
| `Comment` | User comments on articles |
| `ArticleFavorite` | User-article favorite relationships |

### What remained in the main app (User Service)

| Entity | Reason |
|---|---|
| `User` | Core identity entity; authentication, registration, profile management |
| `FollowRelation` | User-to-user relationship, not article-specific |
| JWT/Security | Authentication is a cross-cutting concern owned by the user service |

### Why this boundary

The Article context is the largest and most cohesive domain in the RealWorld app. Articles, tags, comments, and favorites form a natural aggregate — they share the same lifecycle and are always accessed together. The User context is smaller but serves as the identity provider for all services.

This follows DDD's **Bounded Context** pattern: each service owns its data and business rules, communicating across boundaries via well-defined APIs.

---

## 2. Cross-Service Communication

### Pattern: REST over HTTP

The article-service communicates with the main app (user service) via synchronous REST calls using Spring's `RestTemplate`.

**Why REST (not messaging):**
- The RealWorld app's read patterns require synchronous user lookups (e.g., display author info on an article)
- The monolith already uses a request/response pattern
- Simplicity — avoids introducing a message broker for this extraction

### UserServiceClient

The `UserServiceClient` class in article-service makes HTTP calls to the main app:

```
GET /api/users/{userId}        → Fetch user by ID
GET /api/users/username/{name} → Fetch user by username
```

**Resilience:** The client wraps calls in try/catch and returns `Optional.empty()` on failure, allowing the article-service to degrade gracefully if the user service is unavailable.

### User identification

Articles reference their author via `userId` (a string). The article-service stores only the user ID — it does not duplicate user data. Author details are fetched on-demand from the user service when needed for API responses.

---

## 3. Data Ownership

### Separate databases

Each service owns its data exclusively:

- **Main app:** `dev.db` (SQLite) — users, follows, plus legacy article data
- **Article service:** `article-dev.db` (SQLite) — articles, tags, comments, favorites

### Schema design

The article-service schema (Flyway `V1__create_article_tables.sql`) mirrors the relevant tables from the monolith:

- `articles` — core content with `user_id` foreign reference (not a DB FK — cross-service)
- `tags` + `article_tags` — many-to-many tag association
- `article_favorites` — composite key of `article_id` + `user_id`
- `comments` — with `article_id` and `user_id` references

**No cross-database foreign keys.** The `user_id` columns are stored as strings and validated at the application level via the UserServiceClient.

---

## 4. API Design

### Article Service Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/articles` | Create article |
| `GET` | `/api/articles/{slug}` | Get article by slug |
| `PUT` | `/api/articles/{slug}` | Update article |
| `DELETE` | `/api/articles/{slug}` | Delete article |
| `POST` | `/api/articles/{slug}/comments` | Add comment |
| `GET` | `/api/articles/{slug}/comments` | List comments |
| `DELETE` | `/api/articles/{slug}/comments/{id}` | Delete comment |
| `POST` | `/api/articles/{slug}/favorite` | Favorite article |
| `DELETE` | `/api/articles/{slug}/favorite` | Unfavorite article |
| `GET` | `/api/tags` | List all tags |
| `GET` | `/actuator/health` | Health check |

### DTOs

Cross-service communication uses dedicated DTOs to decouple the services:

- `ArticleDto` — Article representation for API responses
- `AuthorDto` — Lightweight user representation (id, username, bio, image)
- `CommentDto` — Comment representation for API responses
- `NewArticleParam` / `UpdateArticleParam` — Article creation/update request bodies
- `NewCommentParam` — Comment creation request body

---

## 5. Infrastructure Decisions

### Docker Compose

Both services run as Docker containers orchestrated by Docker Compose:

- **main-app** (port 8080): The monolith with user management, authentication, and GraphQL
- **article-service** (port 8081): The extracted article microservice

The article-service depends on main-app with `condition: service_healthy`, ensuring the user service is available before article-service starts.

### Health Checks

Both services expose Spring Boot Actuator health endpoints:
- `GET /actuator/health` — Returns service health status
- `GET /actuator/info` — Returns service metadata

These are used by Docker Compose health checks (30s interval, 40s startup grace period).

### SQLite for simplicity

Both services use SQLite for data persistence. This keeps the Docker Compose configuration simple (no separate database container). In production, each service would use its own database instance (PostgreSQL, MySQL, etc.).

---

## 6. What Was NOT Extracted

### GraphQL layer

The DGS GraphQL API remains in the monolith. It provides a unified query interface and would require a GraphQL federation setup to split across services — that's a separate, more complex migration.

### Authentication/Security

JWT validation and user authentication remain in the main app. The article-service trusts the `X-User-Id` header passed from the API gateway or main app. In production, you'd add JWT validation to the article-service or use an API gateway for authentication.

### MyBatis mappers and read services

The monolith's MyBatis-based read services (ArticleQueryService, etc.) with their cursor-based pagination remain in the monolith. The article-service uses JPA-style repository interfaces with Flyway migrations — a clean start rather than porting the complex MyBatis XML mappers.

---

## 7. Future Considerations

1. **API Gateway:** Add Spring Cloud Gateway in front of both services for unified routing and authentication
2. **Service Discovery:** Add Eureka or Consul for dynamic service discovery instead of hardcoded URLs
3. **Event-Driven:** Add an event bus (RabbitMQ/Kafka) for async notifications (e.g., article created → notify followers)
4. **GraphQL Federation:** Split the GraphQL schema using Apollo Federation or DGS Federation
5. **Circuit Breaker:** Add Resilience4j circuit breaker to the UserServiceClient for better fault tolerance
