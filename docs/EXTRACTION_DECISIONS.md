# Extraction Decisions: Article Microservice

This document explains the architectural decisions made during the microservice extraction from the monolith.

## 1. Why "Article" as the Bounded Context

Articles, tags, comments, and favorites form a cohesive aggregate with clear transactional boundaries. They share the same lifecycle and are always accessed together:

- An **Article** owns its Tags, Comments, and Favorites
- CRUD operations on articles cascade to related entities
- Article queries always include tag lists, favorite counts, and author profiles
- Comments are scoped to a single article

This makes the article domain a natural candidate for extraction as a self-contained microservice.

## 2. Why User Stays Separate

User identity, authentication, and follow relationships are a distinct concern:

- Users exist independently of articles
- Authentication (JWT token generation, password management) is a cross-cutting concern
- Follow relationships are user-to-user, not user-to-article
- The user service manages registration, login, and profile operations
- GraphQL/DGS layer is kept in the user-service as an API gateway

## 3. Cross-Cutting Concerns

### Follow-status and Author-profile Lookups

The article-service needs user profile data (username, bio, image) and follow relationships for API responses. These are handled via REST client calls to user-service:

- `GET /api/internal/users/{id}` — Fetch user profile data
- `GET /api/internal/users/{id}/following?targetIds=...` — Batch check follow relationships
- `GET /api/internal/users/{id}/is-following/{targetId}` — Single follow check

**Trade-off**: This introduces network latency and eventual consistency. Article API responses may briefly show stale profile data if a user updates their profile between calls. This is acceptable for a social platform where profile data is not transactionally critical.

**Graceful degradation**: All cross-service calls are wrapped in try-catch blocks. If user-service is unreachable, article queries still return data with `following: false` as the default.

### Users Cache Table

The article-service maintains a local `users` cache table (id, username, bio, image) to support MyBatis SQL joins for profile data in article and comment queries. This avoids N+1 REST calls for list endpoints. The cache is populated/synced from user-service.

## 4. Shared JWT Secret

Both services validate JWTs independently using the same secret key:

- The JWT secret is sourced from the `JWT_SECRET` environment variable
- Both services use the same JJWT 0.12.x library with HS512 signing
- The article-service extracts `userId` from the token and fetches user details from user-service when needed
- Token generation remains solely in user-service (login/register endpoints)

This avoids the complexity of an OAuth2 authorization server while maintaining stateless authentication.

## 5. GraphQL Kept in User-Service

The Netflix DGS GraphQL layer is kept in the main user-service as an API gateway pattern:

- GraphQL provides a unified query interface over both REST APIs
- In a future iteration, GraphQL datafetchers can proxy article queries to article-service
- This avoids duplicating the GraphQL schema and DGS infrastructure in the article-service
- REST remains the primary API for article-service consumers

## 6. Database Per Service

Each service has its own SQLite database with only its relevant tables, enforcing data ownership boundaries:

**User-service database** (`dev.db`):
- `users` — User accounts (id, email, username, password, bio, image)
- `follows` — Follow relationships (user_id, follow_id)
- Article-related tables (retained during transition; will be removed in future cleanup)

**Article-service database** (`article-dev.db`):
- `articles` — Article content and metadata
- `article_tags` / `tags` — Tag associations
- `comments` — Article comments
- `article_favorites` — User favorite associations
- `users` (cache) — Local copy of user profiles for SQL joins

## 7. API Contract: Internal REST API

### User-Service Internal Endpoints

These endpoints are exposed by user-service for article-service consumption. They are permitted without authentication (designed for service-mesh or internal network only).

#### GET /api/internal/users/{id}

Returns user profile data.

**Response** (200 OK):
```json
{
  "id": "uuid-string",
  "username": "jake",
  "bio": "I work at state farm",
  "image": "https://example.com/photo.jpg"
}
```

**Response** (404 Not Found): Empty body

#### GET /api/internal/users/{id}/following?targetIds=id1,id2,id3

Returns the subset of `targetIds` that the user is following. If `targetIds` is omitted, returns all followed user IDs.

**Response** (200 OK):
```json
["target-id-1", "target-id-3"]
```

#### GET /api/internal/users/{id}/is-following/{targetId}

Returns whether the user follows the target.

**Response** (200 OK):
```json
true
```

## 8. Service Configuration

| Property | User-Service | Article-Service |
|---|---|---|
| Port | 8080 | 8081 |
| Database | `dev.db` | `article-dev.db` |
| JWT Secret | `JWT_SECRET` env var | `JWT_SECRET` env var (same) |
| Profiles | `default`, `docker` | `default`, `docker` |

## 9. Docker Compose Architecture

```
┌─────────────────┐     REST (internal)    ┌──────────────────┐
│  user-service   │◄──────────────────────│  article-service  │
│  (port 8080)    │                        │  (port 8081)      │
│                 │                        │                   │
│  - Users API    │                        │  - Articles API   │
│  - Auth/JWT     │                        │  - Comments API   │
│  - Profiles     │                        │  - Tags API       │
│  - GraphQL      │                        │  - Favorites API  │
│  - Internal API │                        │                   │
└─────────────────┘                        └──────────────────┘
```

Article-service depends on user-service being healthy before starting. Both services share the same JWT secret via environment variable.
