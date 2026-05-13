# Extraction Decisions: Article Bounded Context

This document explains the domain boundary choices made when extracting the Article bounded context from the RealWorld monolith into a standalone microservice.

## 1. Domain Boundary Identification

### Article Bounded Context (Extracted)
The following domain concepts were identified as belonging to the Article bounded context:

| Entity | Rationale |
|---|---|
| **Article** | Core aggregate root — articles are the primary content type |
| **Tag** | Article metadata — tags only exist to categorize articles |
| **Comment** | Always scoped to an article — no standalone comment concept |
| **ArticleFavorite** | Join relationship between users and articles, but the favorite count and status are article properties |

### User Bounded Context (Remains in Main App)
The following were intentionally **not** extracted:

| Entity | Rationale |
|---|---|
| **User** | Core identity/auth aggregate — used by all contexts |
| **FollowRelation** | Social graph between users — not article-specific |
| **Profile** | User presentation data — owned by identity context |

### Cross-Cutting Concerns
| Concern | Decision |
|---|---|
| **Authentication (JWT)** | Shared secret between services; main app issues tokens, article-service validates them |
| **Authorization** | Article-service handles its own authorization (article/comment ownership checks) |
| **User Profiles in Responses** | Article-service calls user-service REST API to hydrate author data |

## 2. Service Communication Design

### Pattern: Synchronous REST
We chose synchronous REST over async messaging because:
1. The RealWorld API spec requires immediate consistency — article creation returns the full article with author profile in the same response.
2. The monolith currently has no message broker infrastructure.
3. REST is simpler to implement and debug for a first extraction.

### API Contract: article-service → user-service

#### Get Profile by Username
```
GET /profiles/{username}
Response: { "profile": { "id", "username", "bio", "image", "following" } }
```

#### Get Profile by User ID
```
GET /users/{userId}/profile  
Response: { "profile": { "id", "username", "bio", "image" } }
```

### Cross-Service Authentication
- The article-service receives the user ID via `X-User-Id` HTTP header.
- In a production setup, an API gateway would validate the JWT and inject this header.
- Both services share the same JWT secret for independent token validation.

### Failure Handling
- If user-service is unreachable, article-service returns articles with a fallback author profile (`username: "unknown"`).
- This ensures article reads degrade gracefully rather than failing completely.

## 3. Data Ownership

### Separate Databases
Each service owns its data exclusively:

| Service | Database | Tables |
|---|---|---|
| **user-service** (main app) | `dev.db` | `users`, `follows`, `articles`*, `comments`*, `article_favorites`*, `tags`*, `article_tags`* |
| **article-service** | `article-dev.db` | `articles`, `comments`, `article_favorites`, `tags`, `article_tags` |

*The main app retains article tables for backward compatibility during migration. In a full migration, these would be removed from the main app.

### Foreign Key References
- `articles.user_id` references users, but there is no cross-database foreign key.
- User existence is validated via REST call, not database constraint.
- `article_favorites.user_id` similarly references users by ID without FK constraint.

## 4. API Surface

### article-service Endpoints (port 8081)

| Method | Path | Description | Auth Required |
|---|---|---|---|
| `POST` | `/articles` | Create article | Yes (X-User-Id header) |
| `GET` | `/articles/{slug}` | Get article by slug | No |
| `PUT` | `/articles/{slug}` | Update article | Yes (owner only) |
| `DELETE` | `/articles/{slug}` | Delete article | Yes (owner only) |
| `POST` | `/articles/{slug}/comments` | Add comment | Yes |
| `DELETE` | `/articles/{slug}/comments/{id}` | Delete comment | Yes (author/owner) |
| `POST` | `/articles/{slug}/favorite` | Favorite article | Yes |
| `DELETE` | `/articles/{slug}/favorite` | Unfavorite article | Yes |
| `GET` | `/tags` | List all tags | No |
| `GET` | `/actuator/health` | Health check | No |

### user-service Endpoints (port 8080, unchanged)

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Register user |
| `POST` | `/users/login` | Login |
| `GET` | `/user` | Get current user |
| `PUT` | `/user` | Update current user |
| `GET` | `/profiles/{username}` | Get profile |
| `POST` | `/profiles/{username}/follow` | Follow user |
| `DELETE` | `/profiles/{username}/follow` | Unfollow user |
| `GET` | `/actuator/health` | Health check |

## 5. Infrastructure Decisions

### Docker Compose
- Both services run in separate containers.
- article-service depends on user-service being healthy (via healthcheck).
- Services communicate over Docker's internal network.

### Health Checks
- Both services expose `/actuator/health` via Spring Boot Actuator.
- Docker Compose uses these for dependency ordering and container health monitoring.
- Health endpoint is excluded from authentication requirements.

### Database Choice
- SQLite retained for both services (matching the monolith's choice).
- Each service has its own SQLite database file for data isolation.
- In production, each service would use its own PostgreSQL/MySQL instance.

## 6. What's NOT Included (Future Work)

| Item | Reason for Deferral |
|---|---|
| **API Gateway** | Would add routing complexity; direct service communication is simpler for initial extraction |
| **Service Discovery** | Not needed with Docker Compose service names; would add for Kubernetes deployment |
| **Event-Driven Sync** | Would require message broker; synchronous REST is sufficient for current consistency needs |
| **Circuit Breaker** | User-service fallback is handled in the REST client; Resilience4j could be added for production |
| **Shared Auth Middleware** | Both services validate JWT independently; a shared library could reduce duplication |
| **Article List/Feed Endpoints** | Complex query endpoints remain in monolith; would require read-model replication |
| **GraphQL API** | DGS GraphQL remains in monolith only; article-service exposes REST |
