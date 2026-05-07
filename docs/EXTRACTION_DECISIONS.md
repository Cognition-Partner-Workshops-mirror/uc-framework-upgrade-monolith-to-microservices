# Article Service Extraction Decisions

This document explains the domain boundary choices made when extracting the Article bounded context from the RealWorld monolith into a standalone microservice.

## 1. Bounded Context Identification

### What Was Extracted: Article Bounded Context

The Article bounded context encompasses all functionality related to content creation and consumption:

| Entity | Responsibility | Why It Belongs Here |
|--------|---------------|-------------------|
| **Article** | Core content entity with title, slug, body, description | Primary aggregate root of the content domain |
| **Tag** | Labels attached to articles | Owned exclusively by articles; no independent lifecycle |
| **Comment** | User-authored text attached to articles | Scoped to articles; cannot exist without an article |
| **ArticleFavorite** | Many-to-many relationship between users and articles | Article-centric action; the article-service tracks which articles are favorited |

### What Remains in the Monolith (User Service)

| Entity | Responsibility | Why It Stays |
|--------|---------------|-------------|
| **User** | Authentication, registration, profile management | Core identity domain; referenced by articles but not owned by them |
| **FollowRelation** | Social graph between users | User-to-user relationship; independent of article domain |

## 2. Domain Boundary Rationale

### Coupling Analysis

The monolith's domain has two natural clusters with a clear seam between them:

```
User Cluster                    Article Cluster
─────────────                   ───────────────
User                            Article (references userId)
FollowRelation                  Tag
                                Comment (references userId)
                                ArticleFavorite (references userId)
```

**Cross-boundary references** are limited to `userId` foreign keys in the article tables. This is a textbook candidate for extraction because:

1. **Single direction dependency:** Articles reference users, but users do not reference articles
2. **Loose coupling:** Only `userId` strings cross the boundary — no complex object graphs
3. **Independent lifecycle:** Articles can be created, updated, and deleted without changing user state
4. **High cohesion within the boundary:** Articles, tags, comments, and favorites form a tightly coupled unit that always changes together

### Why Not Extract Users Instead?

Users are the identity provider for the entire system. Every other domain (articles, comments, favorites, follows) references users. Extracting users first would require every remaining service to call the user-service for basic operations, creating more cross-service calls than extracting articles.

## 3. Cross-Service Communication

### Communication Pattern: Synchronous REST

**Choice:** REST over HTTP using `RestTemplate` / `UserServiceClient`

**Rationale:**
- Simple and well-understood pattern
- Matches the existing monolith's synchronous request/response model
- Low operational overhead — no message broker infrastructure needed
- Appropriate for the current scale of the application

**Trade-offs:**
- Introduces runtime coupling between services (article-service depends on user-service being available)
- No eventual consistency guarantees
- In a production system, consider adding circuit breakers (Resilience4j) and caching

### User Identity Propagation

**Choice:** `X-User-Id` HTTP header

The API gateway (or user-service acting as gateway) authenticates the JWT token and passes the resolved `userId` to the article-service via the `X-User-Id` header. This keeps JWT validation in one place and avoids duplicating the JWT secret across services.

```
Client → [JWT] → User-Service → [X-User-Id header] → Article-Service
```

### Profile Resolution

When the article-service needs to populate the `author` field in article/comment responses, it calls the user-service's `/api/users/{userId}/profile` endpoint. This is acceptable because:

1. Profile data changes infrequently
2. Results can be cached
3. The fallback behavior (returning a minimal profile with just the userId) prevents cascade failures

## 4. Data Ownership

### Separate Database per Service

Each service owns its own SQLite database:

| Service | Database | Tables |
|---------|----------|--------|
| User Service (monolith) | `dev.db` | `users`, `follows` |
| Article Service | `article-dev.db` | `articles`, `tags`, `article_tags`, `comments`, `article_favorites` |

**Rationale:**
- Enforces the bounded context boundary at the data level
- Prevents accidental cross-domain queries
- Allows independent schema evolution
- Each service can migrate its database independently

**Note:** The `article_favorites` and `comments` tables contain `user_id` columns that reference users. These are stored as plain strings — there is no foreign key constraint to the user-service's database. This is standard practice in microservices architecture.

## 5. API Contract Between Services

### Article Service → User Service

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/users/{userId}/profile` | GET | Resolve user profile for article/comment author field |
| `/api/profiles/{username}` | GET | Resolve user profile by username |

### User Service → Article Service

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/articles` | GET | List articles (with optional tag/author/favorited filters) |
| `/api/articles/{slug}` | GET/PUT/DELETE | Single article CRUD |
| `/api/articles/{slug}/comments` | GET/POST | Comments on an article |
| `/api/articles/{slug}/comments/{id}` | DELETE | Delete a comment |
| `/api/articles/{slug}/favorite` | POST/DELETE | Favorite/unfavorite an article |
| `/api/tags` | GET | List all tags |
| `/api/health` | GET | Health check |
| `/actuator/health` | GET | Spring Boot Actuator health |

## 6. What Would Come Next

If this extraction were continued to production readiness:

1. **API Gateway:** Add Spring Cloud Gateway or similar to route `/articles/**` to article-service and `/users/**` to user-service
2. **Service Discovery:** Add Eureka or Consul for dynamic service registration
3. **Circuit Breakers:** Add Resilience4j to the UserServiceClient for fault tolerance
4. **Caching:** Cache user profiles in the article-service to reduce cross-service calls
5. **Async Events:** Publish domain events (ArticleCreated, CommentAdded) via RabbitMQ/Kafka for eventual consistency
6. **Shared Library:** Extract common DTOs (ProfileDto, error response format) into a shared library
7. **Contract Testing:** Add Pact or Spring Cloud Contract tests for the API contract between services
8. **Data Migration:** Create a migration script to split the monolith's `dev.db` into service-specific databases
