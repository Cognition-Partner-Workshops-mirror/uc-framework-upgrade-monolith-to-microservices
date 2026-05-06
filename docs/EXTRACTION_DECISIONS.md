# Article Service Extraction Decisions

## Domain Boundary Analysis

### Bounded Contexts Identified

The RealWorld application has two natural bounded contexts:

1. **User Context** — authentication, profiles, follow relationships
2. **Article Context** — articles, tags, comments, favorites

### Why "Article" Was Chosen for Extraction

The Article bounded context was selected because:

1. **Clear domain boundary:** Articles, tags, comments, and favorites form a cohesive aggregate with minimal coupling to the User context.
2. **Single foreign key dependency:** The only cross-context reference is `userId` — articles, comments, and favorites all reference a user by ID but don't need the full User entity.
3. **Independent lifecycle:** Articles can be created, updated, and deleted without modifying user data.
4. **Read-heavy workload:** Article listing, tag queries, and comment retrieval are the most frequent operations, making this service a good candidate for independent scaling.

### What Stays in the Main App (User Service)

- User registration and authentication (JWT issuance)
- Profile management
- Follow/unfollow relationships
- Internal API for cross-service profile lookups

## Cross-Service Communication

### Architecture Pattern

We use **synchronous REST** for cross-service communication via a dedicated internal API:

```
article-service → HTTP GET → user-service /api/internal/profiles/{userId}
article-service → HTTP GET → user-service /api/internal/profiles/by-username/{username}
article-service → HTTP GET → user-service /api/internal/users/{userId}/following/{targetUserId}
```

### Why REST Over Events

- **Simplicity:** The RealWorld app is request-response oriented; eventual consistency would add unnecessary complexity.
- **Low volume:** Profile lookups happen per-request but can be cached if needed.
- **Operational simplicity:** No message broker infrastructure required for a demo/learning application.

### Authentication Between Services

The article-service receives the authenticated user's ID via an `X-User-Id` header (set by an API gateway or upstream proxy). Internal service-to-service calls (`/api/internal/**`) are unauthenticated within the trusted network.

## Data Ownership

| Entity | Owner Service | Referenced By |
|--------|--------------|---------------|
| User | user-service | article-service (by userId) |
| FollowRelation | user-service | article-service (via REST) |
| Article | article-service | — |
| Tag | article-service | — |
| Comment | article-service | — |
| ArticleFavorite | article-service | — |

## API Contracts

### Article Service API (port 8081)

| Method | Path | Description |
|--------|------|-------------|
| POST | /articles | Create article |
| GET | /articles | List articles |
| GET | /articles/{slug} | Get article |
| PUT | /articles/{slug} | Update article |
| DELETE | /articles/{slug} | Delete article |
| POST | /articles/{slug}/comments | Add comment |
| DELETE | /articles/{slug}/comments/{id} | Delete comment |
| POST | /articles/{slug}/favorite | Favorite article |
| DELETE | /articles/{slug}/favorite | Unfavorite article |
| GET | /tags | List all tags |
| GET | /health | Service health check |
| GET | /actuator/health | Spring Actuator health |

### User Service Internal API (port 8080)

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/internal/profiles/{userId} | Get profile by user ID |
| GET | /api/internal/profiles/by-username/{username} | Get profile by username |
| GET | /api/internal/users/{userId}/following/{targetUserId} | Check follow status |

## Docker Compose Topology

```
┌─────────────────┐         ┌──────────────────┐
│  user-service   │◄────────│  article-service │
│  (port 8080)    │  REST   │  (port 8081)     │
│                 │         │                  │
│  - Auth/JWT     │         │  - Articles      │
│  - Profiles     │         │  - Comments      │
│  - Follows      │         │  - Tags          │
│  - Internal API │         │  - Favorites     │
└─────────────────┘         └──────────────────┘
```

Both services use SQLite for simplicity in development but can be independently migrated to PostgreSQL or other databases in production.

## Future Considerations

1. **API Gateway:** Add an API gateway (e.g., Spring Cloud Gateway) for routing, authentication header injection, and rate limiting.
2. **Service Discovery:** Add Eureka or Consul if more services are extracted.
3. **Caching:** Add Redis caching for profile lookups in article-service.
4. **Event-Driven:** Consider publishing domain events (ArticleCreated, CommentAdded) for analytics or notifications.
5. **Shared Library:** Extract common DTOs (ProfileData, pagination) into a shared Maven module.
