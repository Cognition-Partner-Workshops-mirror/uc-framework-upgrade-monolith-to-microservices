# Extraction Decisions: Article Microservice

This document explains the domain boundary choices made when extracting the Article bounded context from the monolith.

## 1. Why the Article Bounded Context?

The Article domain was chosen for extraction because:

1. **Clear domain boundary:** Articles, comments, tags, and favorites form a cohesive aggregate with minimal coupling to the User domain. The only link is `userId` (a string reference), not a direct object dependency.
2. **Highest complexity:** The Article context contains the most business logic (CRUD, favorites, tags, comments, feed generation), making it the best candidate for independent scaling.
3. **Independent data model:** Article-related tables (`articles`, `comments`, `tags`, `article_tags`, `article_favorites`) have no foreign key constraints to `users` — they reference users by ID only.
4. **Read-heavy workload:** Article listing, search, and feed endpoints are read-heavy and could benefit from independent scaling and caching strategies.

## 2. Domain Boundary Definition

### What moved to article-service:

| Layer | Components |
|---|---|
| **Domain entities** | `Article`, `Tag`, `Comment`, `ArticleFavorite` |
| **Repositories** | `ArticleRepository`, `CommentRepository`, `ArticleFavoriteRepository` |
| **REST APIs** | `ArticlesApi`, `ArticleApi`, `CommentsApi`, `ArticleFavoriteApi`, `TagsApi` |
| **MyBatis mappers** | `ArticleMapper`, `CommentMapper`, `ArticleFavoriteMapper`, `TagReadService` |
| **Database tables** | `articles`, `comments`, `tags`, `article_tags`, `article_favorites` |

### What stays in user-service (monolith):

| Layer | Components |
|---|---|
| **Domain entities** | `User`, `FollowRelation` |
| **Services** | `UserService`, `JwtService`, `ProfileQueryService` |
| **REST APIs** | `UsersApi`, `CurrentUserApi`, `ProfileApi` |
| **GraphQL** | All DGS datafetchers and mutations (kept in monolith for now) |
| **Database tables** | `users`, `follows` |

### What is shared:

| Component | Sharing Strategy |
|---|---|
| JWT secret | Configured via environment variable in both services |
| `ProfileData` DTO | Duplicated in article-service (cross-service DTO) |
| `DateTimeHandler` | Duplicated (MyBatis type handler for Joda DateTime) |

## 3. Inter-Service Communication

### Pattern: REST Client (Synchronous HTTP)
The article-service communicates with the user-service via `UserServiceClient`, a REST client using Spring's `RestTemplate`.

### API Contracts

**article-service → user-service:**

| Endpoint | Method | Purpose | Request | Response |
|---|---|---|---|---|
| `/profiles/{username}` | GET | Get user profile for article author display | Path: username | `{ "profile": { "username", "bio", "image", "following" } }` |
| `/internal/users/{userId}/profile` | GET | Get profile by internal userId | Path: userId | `{ "username", "bio", "image" }` |

### Why REST over messaging?
- **Simplicity:** REST is synchronous and easier to debug for the initial extraction.
- **Consistency:** The monolith already uses a REST API; keeping the same protocol minimizes architectural changes.
- **Future evolution:** Can be replaced with async messaging (e.g., Kafka) or gRPC if latency becomes an issue.

## 4. Authentication Strategy

Both services share the same JWT secret and use identical token validation logic:
- User-service issues JWT tokens at login
- Article-service validates tokens using the same HMAC-SHA512 key
- Article-service extracts `userId` from the JWT subject claim
- No need for an auth service or token exchange — simple shared-secret approach

This works well for the initial extraction. For a more complex multi-service architecture, consider:
- Centralized auth service (OAuth2/OIDC)
- Token introspection endpoint
- API gateway handling authentication

## 5. Database Strategy

### Separate Databases (Database per Service)
Each service has its own SQLite database:
- `user-service` → `dev.db` (users, follows)
- `article-service` → `article-dev.db` (articles, comments, tags, favorites)

### Migration Approach
- Article-service uses Flyway with its own migration: `V1__create_article_tables.sql`
- Only article-related tables are created in the article-service database
- User references are stored as `user_id` (VARCHAR) with no FK constraint

## 6. API Backward Compatibility

The article-service exposes the same REST API paths as the monolith:
- `GET /articles` — list articles
- `GET /articles/{slug}` — get single article
- `POST /articles` — create article
- `PUT /articles/{slug}` — update article
- `DELETE /articles/{slug}` — delete article
- `GET /articles/feed` — user feed
- `POST /articles/{slug}/comments` — add comment
- `DELETE /articles/{slug}/comments/{id}` — delete comment
- `POST /articles/{slug}/favorite` — favorite article
- `DELETE /articles/{slug}/favorite` — unfavorite article
- `GET /tags` — list all tags
- `GET /actuator/health` — health check

Clients can switch between monolith and microservice by changing the base URL.

## 7. What Was NOT Extracted (and Why)

### GraphQL Layer
The DGS GraphQL layer was kept in the monolith because:
- GraphQL datafetchers aggregate data from multiple domains (articles + users + profiles)
- Splitting GraphQL across services would require schema stitching or federation
- The GraphQL layer can be extracted later as an API gateway

### Feed Generation
The `/articles/feed` endpoint requires knowledge of followed users (managed by user-service). In the article-service, this is simplified. A production implementation would:
1. Call user-service to get the list of followed user IDs
2. Query articles by those user IDs

### Profile Enrichment
The monolith's `ArticleQueryService` enriches articles with author profile data and follow status. The article-service uses `UserServiceClient` to fetch this data via REST when needed.

## 8. Docker Compose Architecture

```
┌─────────────────┐     REST (HTTP)     ┌──────────────────┐
│  user-service    │◄───────────────────│  article-service  │
│  (monolith)      │                     │  (microservice)   │
│  Port: 8080      │                     │  Port: 8081       │
│  DB: dev.db      │                     │  DB: article-dev  │
└─────────────────┘                     └──────────────────┘
         │                                        │
         └────────── realworld-network ───────────┘
```

Both services:
- Share the same Docker network for inter-service communication
- Have health checks via Spring Boot Actuator
- Use the same JWT secret (configured via environment variable)
- article-service depends on user-service being healthy before starting

## 9. Future Improvements

1. **API Gateway:** Add an API gateway (e.g., Spring Cloud Gateway) to route requests and handle cross-cutting concerns
2. **Service Discovery:** Add Eureka or Consul for dynamic service discovery
3. **Async Communication:** Replace synchronous REST calls with event-driven messaging for profile updates
4. **GraphQL Federation:** Extract GraphQL layer as a federated gateway
5. **Shared Library:** Extract common DTOs and utilities into a shared Maven/Gradle module
6. **Observability:** Add distributed tracing (Zipkin/Jaeger) and centralized logging (ELK)
