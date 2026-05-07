# Microservice Extraction Decisions

## Overview
This document explains the domain boundary choices made when extracting the Article bounded context from the RealWorld monolith into a standalone microservice.

---

## 1. Why "Article" as the First Bounded Context?

### Domain Analysis
The RealWorld application has two natural bounded contexts:

1. **User Context** — User registration, authentication, profiles, follow relationships
2. **Article Context** — Articles, tags, comments, favorites

### Why Article First?
- **Highest entity count:** Articles, Tags, Comments, Favorites — four distinct entity types
- **Clear data ownership:** Article data is self-contained; articles reference users by ID only
- **Unidirectional dependency:** Articles depend on Users (need author info), but Users do not depend on Articles for core functionality
- **Independent scalability:** Article reads typically dominate traffic in a blogging platform

---

## 2. Domain Boundary Definition

### What Moved to `article-service/`

| Entity | Reason |
|---|---|
| `Article` | Core of the bounded context |
| `Tag` | Belongs exclusively to articles |
| `Comment` | Always associated with an article |
| `ArticleFavorite` | Join between articles and users, owned by article context |
| `ArticleRepository` | Persistence for articles |
| `CommentRepository` | Persistence for comments |
| `ArticleFavoriteRepository` | Persistence for favorites |

### What Remains in the Monolith (User Service)

| Entity | Reason |
|---|---|
| `User` | Core identity — referenced by many contexts |
| `FollowRelation` | User-to-user relationship, no article dependency |
| `UserRepository` | User persistence |
| `JwtService` | Authentication — cross-cutting concern |
| `WebSecurityConfig` | Security — stays with the gateway/user service |
| `ProfileApi` | User profiles — belongs to user context |

### Database Tables

**article-service owns:**
- `articles` — Article content and metadata
- `tags` — Tag definitions
- `article_tags` — Article-tag associations
- `comments` — Article comments
- `article_favorites` — User-article favorite associations

**main app (user-service) owns:**
- `users` — User accounts
- `follows` — Follow relationships

---

## 3. Cross-Service Communication

### Communication Pattern: Synchronous REST

The article-service communicates with the user-service via REST API calls using Spring's `RestTemplate`.

### Why REST (not messaging)?
- **Simplicity:** The RealWorld spec is request-response oriented
- **Data needs:** Article views need author info synchronously (user profile displayed with each article)
- **Consistency:** Both services share the same request lifecycle

### API Contract

**article-service → user-service:**
- `GET /api/users/{userId}` — Fetch author details by user ID
- `GET /api/users/username/{username}` — Fetch user by username

**User identity propagation:**
- The `X-User-Id` header is passed from the API gateway/client to identify the authenticated user
- Article-service uses this header for write operations (create article, add comment, favorite)

### Future Considerations
- Add circuit breaker (Resilience4j) for user-service calls
- Consider caching user profiles in article-service to reduce cross-service calls
- Evaluate event-driven communication (e.g., user profile updated → article-service cache invalidation)

---

## 4. What Stays Shared

### GraphQL Layer
The GraphQL (DGS) layer remains in the monolith. It acts as a Backend-for-Frontend (BFF) that:
- Aggregates data from both contexts
- Handles cursor-based pagination
- Manages the schema stitching

### Exception Handling
Each service has its own exception handling. The monolith retains its existing exception handlers for the REST and GraphQL APIs.

### Authentication
JWT authentication stays in the monolith/user-service. The article-service trusts the `X-User-Id` header for authenticated requests, following the sidecar/gateway pattern.

---

## 5. Data Consistency Strategy

### Eventual Consistency Accepted
- Article favorites reference `userId` but don't enforce a foreign key to the users table
- If a user is deleted, their articles and comments become orphaned (acceptable for this domain)
- Future improvement: event-driven cleanup when users are deleted

### No Distributed Transactions
- Each service owns its own SQLite database
- No two-phase commit needed — operations are either fully local to one service or can tolerate eventual consistency

---

## 6. Service Configuration

| Property | Main App (User Service) | Article Service |
|---|---|---|
| Port | 8080 | 8081 |
| Database | `dev.db` (SQLite) | `article-dev.db` (SQLite) |
| Health Check | `/actuator/health` | `/actuator/health` |
| API Prefix | `/users`, `/profiles`, `/tags` | `/api/articles`, `/api/tags`, `/api/articles/{slug}/comments`, `/api/articles/{slug}/favorite` |

---

## 7. Docker Compose Topology

```
┌─────────────────────────────────────────────┐
│                 Docker Network               │
│                                              │
│  ┌──────────────┐    ┌───────────────────┐  │
│  │  main-app    │◄───│  article-service  │  │
│  │  (port 8080) │    │  (port 8081)      │  │
│  │              │    │                    │  │
│  │  - Users     │    │  - Articles       │  │
│  │  - Profiles  │    │  - Comments       │  │
│  │  - Auth/JWT  │    │  - Tags           │  │
│  │  - GraphQL   │    │  - Favorites      │  │
│  │  - Security  │    │                    │  │
│  └──────────────┘    └───────────────────┘  │
│       │                      │               │
│  ┌────┴─────┐         ┌─────┴──────┐       │
│  │  dev.db  │         │article-dev │       │
│  │ (SQLite) │         │   .db      │       │
│  └──────────┘         └────────────┘       │
└─────────────────────────────────────────────┘
```

---

## 8. API Contracts Between Services

### Article Service Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/articles` | Create a new article |
| `GET` | `/api/articles/{slug}` | Get article by slug |
| `PUT` | `/api/articles/{slug}` | Update article |
| `DELETE` | `/api/articles/{slug}` | Delete article |
| `POST` | `/api/articles/{slug}/comments` | Add comment |
| `GET` | `/api/articles/{slug}/comments/{id}` | Get comment |
| `DELETE` | `/api/articles/{slug}/comments/{id}` | Delete comment |
| `POST` | `/api/articles/{slug}/favorite` | Favorite article |
| `DELETE` | `/api/articles/{slug}/favorite` | Unfavorite article |
| `GET` | `/api/tags` | List all tags |
| `GET` | `/actuator/health` | Health check |

### User Service Endpoints (consumed by article-service)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/users/{userId}` | Get user by ID |
| `GET` | `/api/users/username/{username}` | Get user by username |
| `GET` | `/actuator/health` | Health check |
