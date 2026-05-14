# Microservice Extraction Decisions

This document explains the domain boundary choices made when extracting the Article bounded context from the RealWorld monolith into a standalone microservice.

## 1. Bounded Context Identification

### Domain Analysis

The RealWorld blogging platform has two primary bounded contexts:

1. **User Context**: User registration, authentication, profiles, follow relationships
2. **Article Context**: Articles, tags, comments, favorites

These contexts are loosely coupled — the Article context references users only by ID (foreign key) and never modifies user state directly.

### Why "Article" Was Chosen for Extraction

| Factor | User Context | Article Context |
|---|---|---|
| **Domain Cohesion** | High (auth + profiles) | High (articles + tags + comments + favorites) |
| **External Dependencies** | Minimal (self-contained) | Needs user profiles for display |
| **Change Frequency** | Low (auth is stable) | High (content features evolve rapidly) |
| **Scaling Needs** | Low (auth is lightweight) | High (read-heavy content serving) |
| **Data Volume** | Small (user accounts) | Large (articles, comments grow unbounded) |

The Article context was chosen because:
- It represents the majority of the domain logic and API surface
- Content serving typically requires independent scaling
- The coupling to the User context is minimal and well-defined (user IDs only)
- The User context naturally stays in the monolith as the authentication provider

## 2. What Was Extracted

### Entities Moved to article-service

| Entity | Package | Reason |
|---|---|---|
| `Article` | `core.article` | Core article domain entity |
| `Tag` | `core.article` | Tags belong to articles |
| `Comment` | `core.comment` | Comments are on articles |
| `ArticleFavorite` | `core.favorite` | Favorites are on articles |

### API Endpoints Moved

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/articles` | Create article |
| `GET` | `/articles/{slug}` | Get article by slug |
| `PUT` | `/articles/{slug}` | Update article |
| `DELETE` | `/articles/{slug}` | Delete article |
| `POST` | `/articles/{slug}/favorite` | Favorite article |
| `DELETE` | `/articles/{slug}/favorite` | Unfavorite article |
| `POST` | `/articles/{slug}/comments` | Add comment |
| `GET` | `/articles/{slug}/comments` | List comments |
| `DELETE` | `/articles/{slug}/comments/{id}` | Delete comment |
| `GET` | `/tags` | List all tags |

### What Stays in the Monolith (User-Service)

| Entity/Endpoint | Reason |
|---|---|
| `User` entity | Core authentication domain |
| `FollowRelation` entity | User-to-user relationship |
| `POST /users` | Registration |
| `POST /users/login` | Authentication |
| `GET /user` | Current user |
| `PUT /user` | Update user |
| `GET /profiles/{username}` | User profiles |
| `POST /profiles/{username}/follow` | Follow user |
| `DELETE /profiles/{username}/follow` | Unfollow user |
| GraphQL API | Remains in monolith for now |

## 3. Cross-Service Communication

### Pattern: Synchronous REST

The article-service communicates with the user-service (monolith) via synchronous REST calls.

**Why REST (not messaging)**:
- The RealWorld API requires immediate consistency (article response includes author profile)
- The communication pattern is simple request/response (get user profile by ID)
- Async messaging would add unnecessary complexity for this use case

### Internal API Contract

A new internal API was added to the monolith for cross-service use:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/internal/users/{userId}` | Get user by ID |
| `GET` | `/api/internal/users/by-username/{username}` | Get user by username |

**Response Format**:
```json
{
  "id": "user-uuid",
  "username": "john",
  "bio": "I work at State Farm",
  "image": "https://example.com/avatar.jpg"
}
```

### Graceful Degradation

If the user-service is unavailable, the article-service returns a default profile:
```json
{
  "id": "user-uuid",
  "username": "unknown",
  "bio": null,
  "image": null
}
```

This ensures article reads are not blocked by user-service outages.

## 4. Authentication Strategy

### Shared JWT Secret

Both services share the same JWT secret key configured in `application.properties`. This means:
- Tokens issued by the user-service are valid in the article-service
- No token exchange or delegation is needed
- The article-service validates tokens locally without calling the user-service

### Security Tradeoff

**Pros**: Simple, no additional network calls for auth, low latency
**Cons**: Secret must be kept in sync between services

**Future improvement**: Use an asymmetric key pair (RS256) so the user-service signs tokens with a private key and the article-service verifies with a public key. This eliminates shared secret management.

## 5. Database Strategy

### Separate Databases (Database-per-Service)

Each service has its own SQLite database:
- **user-service**: `dev.db` (users, follows)
- **article-service**: `article-dev.db` (articles, tags, comments, favorites)

**Why separate databases**:
- Enforces true bounded context isolation
- Each service owns its data and schema
- Independent Flyway migrations per service
- No cross-database joins or foreign key constraints

### Data Ownership

| Table | Owner | Notes |
|---|---|---|
| `users` | user-service | Source of truth for user data |
| `follows` | user-service | User-to-user relationships |
| `articles` | article-service | `user_id` column stores reference (not FK) |
| `tags` | article-service | |
| `article_tags` | article-service | |
| `article_favorites` | article-service | `user_id` column stores reference (not FK) |
| `comments` | article-service | `user_id` column stores reference (not FK) |

## 6. Docker Compose Architecture

```
┌──────────────────────┐     REST      ┌──────────────────────┐
│    article-service    │──────────────>│    user-service       │
│    (port 8081)        │  /api/internal│    (port 8080)        │
│                       │   /users/{id} │                       │
│  - Articles API       │              │  - Users API           │
│  - Comments API       │              │  - Profiles API        │
│  - Tags API           │              │  - Auth (JWT)          │
│  - Favorites API      │              │  - GraphQL API         │
│                       │              │  - Internal User API   │
│  [article-dev.db]     │              │  [dev.db]              │
└──────────────────────┘              └──────────────────────┘
```

### Service Dependencies
- `article-service` depends on `user-service` (for profile resolution)
- `user-service` has no dependency on `article-service`
- Both services expose `/actuator/health` for health checks
- Docker Compose uses `depends_on` with `condition: service_healthy`

## 7. What Was NOT Extracted (and Why)

### GraphQL API
The GraphQL API (Netflix DGS) remains in the monolith because:
- It aggregates data from both user and article domains
- Moving it would require implementing a GraphQL gateway/federation
- This is a separate concern better addressed as a dedicated API gateway in the future

### Feed Endpoint
The article feed (`GET /articles/feed`) requires knowledge of who the current user follows, which is user-service data. In a full extraction, this would need:
- A follow-service event to notify article-service of follow changes
- Or a synchronous call to user-service to get the follow list

For now, the feed endpoint exists in both services to maintain API compatibility.

## 8. Future Improvements

1. **API Gateway**: Add an API gateway (e.g., Spring Cloud Gateway) to route requests to the correct service
2. **Event-Driven Communication**: Use events (e.g., via RabbitMQ) for eventually consistent operations
3. **Service Discovery**: Add Eureka or Consul for dynamic service discovery
4. **Asymmetric JWT**: Switch from shared secret to public/private key pair
5. **Contract Testing**: Add Pact or Spring Cloud Contract tests for the internal API
6. **Distributed Tracing**: Add Micrometer Tracing for cross-service observability
