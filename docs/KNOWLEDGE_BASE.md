# Knowledge Base — RealWorld "Conduit" Application

## 1. Architecture Overview

### System Summary

This is a full-stack implementation of the [RealWorld "Conduit" specification](https://github.com/gothinkster/realworld) — a Medium.com clone that provides a production-grade example of a Java/Spring Boot backend paired with a Next.js/React frontend.

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend Framework | Spring Boot | 2.6.3 |
| Language (Backend) | Java | 11 (source/target) |
| Persistence | MyBatis + SQLite | mybatis-spring-boot 2.2.2 / sqlite-jdbc 3.36.0.3 |
| Database Migrations | Flyway | managed by Spring Boot 2.6.3 BOM |
| GraphQL | Netflix DGS Framework | 4.9.21 |
| Authentication | JWT (jjwt) | 0.11.2 |
| Frontend Framework | Next.js + React | Next 9.5.1 / React 16.13.1 |
| HTTP Client (Frontend) | Axios | 0.19.2 |
| Code Formatting | Spotless (Google Java Format) | 6.2.1 |
| Test Coverage | JaCoCo | 0.8.7 |
| E2E Testing | Selenium + TestNG | Selenium 4.15.0 / TestNG 7.8.0 |
| Build System | Gradle (Wrapper) | 7.4 |

### Package Structure (Backend — `src/main/java/io/spring/`)

```
io.spring
├── RealWorldApplication.java        # Spring Boot main class
├── JacksonCustomizations.java       # Custom Joda DateTime JSON serializer
├── MyBatisConfig.java               # MyBatis configuration
├── Util.java                        # String utility
├── api/                             # REST controllers (Web Layer)
│   ├── ArticleApi.java              # GET/PUT/DELETE /articles/{slug}
│   ├── ArticlesApi.java             # POST /articles, GET /articles, GET /articles/feed
│   ├── ArticleFavoriteApi.java      # POST/DELETE /articles/{slug}/favorite
│   ├── CommentsApi.java             # POST/GET/DELETE /articles/{slug}/comments
│   ├── CurrentUserApi.java          # GET/PUT /user
│   ├── ProfileApi.java              # GET/POST/DELETE /profiles/{username}/follow
│   ├── TagsApi.java                 # GET /tags
│   ├── UsersApi.java                # POST /users, POST /users/login
│   ├── exception/                   # Exception handlers & custom exceptions
│   │   ├── CustomizeExceptionHandler.java   # @RestControllerAdvice
│   │   ├── ErrorResource.java
│   │   ├── ErrorResourceSerializer.java
│   │   ├── FieldErrorResource.java
│   │   ├── InvalidAuthenticationException.java
│   │   ├── InvalidRequestException.java
│   │   ├── NoAuthorizationException.java    # 403
│   │   └── ResourceNotFoundException.java   # 404
│   └── security/
│       ├── JwtTokenFilter.java      # OncePerRequestFilter for JWT
│       └── WebSecurityConfig.java   # Spring Security config
├── application/                     # Query services, DTOs, command services
│   ├── ArticleQueryService.java     # Read-side article queries (CQRS)
│   ├── CommentQueryService.java     # Read-side comment queries
│   ├── ProfileQueryService.java     # Read-side profile queries
│   ├── TagsQueryService.java        # Tag listing
│   ├── UserQueryService.java        # User data lookup
│   ├── CursorPager.java / CursorPageParameter.java / DateTimeCursor.java  # Cursor pagination
│   ├── Page.java                    # Offset pagination
│   ├── article/
│   │   ├── ArticleCommandService.java         # Create/update article
│   │   ├── NewArticleParam.java               # Validated create DTO
│   │   ├── UpdateArticleParam.java            # Validated update DTO
│   │   ├── DuplicatedArticleConstraint.java   # Custom validation
│   │   └── DuplicatedArticleValidator.java
│   ├── data/                        # Read-model DTOs
│   │   ├── ArticleData.java
│   │   ├── ArticleDataList.java
│   │   ├── ArticleFavoriteCount.java
│   │   ├── CommentData.java
│   │   ├── ProfileData.java
│   │   ├── UserData.java
│   │   └── UserWithToken.java
│   └── user/
│       ├── UserService.java                   # Create/update user
│       ├── RegisterParam.java                 # Validated registration DTO
│       ├── UpdateUserParam.java / UpdateUserCommand.java
│       ├── DuplicatedEmailConstraint.java / DuplicatedEmailValidator.java
│       └── DuplicatedUsernameConstraint.java / DuplicatedUsernameValidator.java
├── core/                            # Domain entities & repository interfaces (DDD)
│   ├── article/
│   │   ├── Article.java             # Aggregate root
│   │   ├── ArticleRepository.java   # Interface
│   │   └── Tag.java                 # Value object
│   ├── comment/
│   │   ├── Comment.java
│   │   └── CommentRepository.java
│   ├── favorite/
│   │   ├── ArticleFavorite.java
│   │   └── ArticleFavoriteRepository.java
│   ├── service/
│   │   ├── AuthorizationService.java  # Static ownership checks
│   │   └── JwtService.java            # Interface
│   └── user/
│       ├── User.java
│       ├── UserRepository.java
│       └── FollowRelation.java
├── graphql/                         # Netflix DGS GraphQL layer
│   ├── ArticleDatafetcher.java      # Queries: article, articles, feed
│   ├── ArticleMutation.java         # Mutations: createArticle, updateArticle, etc.
│   ├── CommentDatafetcher.java      # Comment queries
│   ├── CommentMutation.java         # addComment, deleteComment
│   ├── MeDatafetcher.java           # me query
│   ├── ProfileDatafetcher.java      # profile query
│   ├── RelationMutation.java        # followUser, unfollowUser
│   ├── SecurityUtil.java            # Extract current user from SecurityContext
│   ├── TagDatafetcher.java          # tags query
│   ├── UserMutation.java            # createUser, login, updateUser
│   └── exception/
│       ├── AuthenticationException.java
│       └── GraphQLCustomizeExceptionHandler.java
└── infrastructure/                  # MyBatis implementations
    ├── mybatis/
    │   ├── DateTimeHandler.java     # Joda DateTime TypeHandler
    │   ├── mapper/                  # MyBatis mapper interfaces
    │   │   ├── ArticleFavoriteMapper.java
    │   │   ├── ArticleMapper.java
    │   │   ├── CommentMapper.java
    │   │   └── UserMapper.java
    │   └── readservice/             # Read-side MyBatis mappers (CQRS)
    │       ├── ArticleFavoritesReadService.java
    │       ├── ArticleReadService.java
    │       ├── CommentReadService.java
    │       ├── TagReadService.java
    │       ├── UserReadService.java
    │       └── UserRelationshipQueryService.java
    ├── repository/                  # Repository implementations
    │   ├── MyBatisArticleRepository.java
    │   ├── MyBatisArticleFavoriteRepository.java
    │   ├── MyBatisCommentRepository.java
    │   └── MyBatisUserRepository.java
    └── service/
        └── DefaultJwtService.java   # JWT implementation (HS512)
```

### Communication Pattern

This is a **monolithic** application. The backend serves both a REST API and a GraphQL API from a single deployable Spring Boot JAR. The Next.js frontend communicates with the backend over HTTP (REST) at `http://localhost:8080`.

```
┌──────────────────┐        HTTP (REST)          ┌───────────────────────────────┐
│   Next.js (3000) │ ─────────────────────────▶  │  Spring Boot Backend (8080)   │
│   React SPA      │                              │                               │
└──────────────────┘                              │  ┌───────┐    ┌───────────┐  │
                                                  │  │ REST  │    │ GraphQL   │  │
         GraphQL clients ──────────────────────▶  │  │ /api  │    │ /graphql  │  │
                                                  │  └───┬───┘    └─────┬─────┘  │
                                                  │      │              │         │
                                                  │  ┌───▼──────────────▼─────┐  │
                                                  │  │   Application Layer     │  │
                                                  │  │  (Query/Command Svc)    │  │
                                                  │  └──────────┬─────────────┘  │
                                                  │             │                │
                                                  │  ┌──────────▼─────────────┐  │
                                                  │  │   Core Domain Layer     │  │
                                                  │  │  (Entities, Repos)      │  │
                                                  │  └──────────┬─────────────┘  │
                                                  │             │                │
                                                  │  ┌──────────▼─────────────┐  │
                                                  │  │   Infrastructure        │  │
                                                  │  │  (MyBatis + SQLite)     │  │
                                                  │  └────────────────────────┘  │
                                                  └───────────────────────────────┘
```

---

## 2. Data Models

### Entity–Relationship Diagram (Logical)

```
┌──────────┐       ┌─────────┐       ┌──────────────────┐
│  users   │──1:N──│ articles │──M:N──│ tags             │
│          │       │         │       │ (via article_tags)│
└────┬─────┘       └────┬────┘       └──────────────────┘
     │                  │
     │ M:N (follows)    │ 1:N
     │                  │
┌────▼─────┐       ┌────▼────┐
│  follows │       │comments │
└──────────┘       └─────────┘
     │                  
     │ M:N (article_favorites)
     │
┌────▼───────────────┐
│ article_favorites  │
└────────────────────┘
```

### Table Definitions

| Table | Column | Type | Constraints |
|-------|--------|------|-------------|
| **users** | `id` | varchar(255) | PK |
| | `username` | varchar(255) | UNIQUE |
| | `email` | varchar(255) | UNIQUE |
| | `password` | varchar(255) | BCrypt-hashed |
| | `bio` | text | |
| | `image` | varchar(511) | |
| **articles** | `id` | varchar(255) | PK |
| | `user_id` | varchar(255) | FK → users.id (implicit) |
| | `slug` | varchar(255) | UNIQUE |
| | `title` | varchar(255) | |
| | `description` | text | |
| | `body` | text | |
| | `created_at` | TIMESTAMP | NOT NULL |
| | `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| **tags** | `id` | varchar(255) | PK |
| | `name` | varchar(255) | NOT NULL |
| **article_tags** | `article_id` | varchar(255) | Composite: article_id + tag_id |
| | `tag_id` | varchar(255) | |
| **comments** | `id` | varchar(255) | PK |
| | `body` | text | |
| | `article_id` | varchar(255) | FK → articles.id (implicit) |
| | `user_id` | varchar(255) | FK → users.id (implicit) |
| | `created_at` | TIMESTAMP | NOT NULL |
| | `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| **article_favorites** | `article_id` | varchar(255) | Composite PK |
| | `user_id` | varchar(255) | Composite PK |
| **follows** | `user_id` | varchar(255) | |
| | `follow_id` | varchar(255) | |

### Domain Entities (Java)

| Entity | Package | Key Fields | Notes |
|--------|---------|-----------|-------|
| `User` | `core.user` | id, email, username, password, bio, image | UUID-based ID, BCrypt password |
| `Article` | `core.article` | id, userId, slug, title, description, body, tags, createdAt, updatedAt | Slug auto-generated from title, Joda DateTime |
| `Tag` | `core.article` | id, name | UUID-based ID |
| `Comment` | `core.comment` | id, body, userId, articleId, createdAt | UUID-based ID |
| `ArticleFavorite` | `core.favorite` | articleId, userId | Join entity |
| `FollowRelation` | `core.user` | userId, targetId | User-to-user follow |

---

## 3. API Surface Map

### REST API Endpoints

| Method | Path | Auth | Controller | Description |
|--------|------|------|-----------|-------------|
| `POST` | `/users` | Public | `UsersApi` | Register new user |
| `POST` | `/users/login` | Public | `UsersApi` | Login (returns JWT) |
| `GET` | `/user` | Required | `CurrentUserApi` | Get current user profile |
| `PUT` | `/user` | Required | `CurrentUserApi` | Update current user |
| `GET` | `/profiles/{username}` | Optional | `ProfileApi` | Get user profile |
| `POST` | `/profiles/{username}/follow` | Required | `ProfileApi` | Follow user |
| `DELETE` | `/profiles/{username}/follow` | Required | `ProfileApi` | Unfollow user |
| `GET` | `/articles` | Optional | `ArticlesApi` | List articles (filter by tag, author, favorited; offset/limit pagination) |
| `GET` | `/articles/feed` | Required | `ArticlesApi` | Feed of followed users' articles |
| `POST` | `/articles` | Required | `ArticlesApi` | Create article |
| `GET` | `/articles/{slug}` | Optional | `ArticleApi` | Get single article |
| `PUT` | `/articles/{slug}` | Required | `ArticleApi` | Update article (author only) |
| `DELETE` | `/articles/{slug}` | Required | `ArticleApi` | Delete article (author only) |
| `POST` | `/articles/{slug}/favorite` | Required | `ArticleFavoriteApi` | Favorite article |
| `DELETE` | `/articles/{slug}/favorite` | Required | `ArticleFavoriteApi` | Unfavorite article |
| `GET` | `/articles/{slug}/comments` | Optional | `CommentsApi` | List comments on article |
| `POST` | `/articles/{slug}/comments` | Required | `CommentsApi` | Add comment |
| `DELETE` | `/articles/{slug}/comments/{id}` | Required | `CommentsApi` | Delete comment (author/article owner) |
| `GET` | `/tags` | Public | `TagsApi` | List all tags |

### GraphQL API (`/graphql`)

**Queries:**
- `article(slug: String!)` → Article
- `articles(first, after, last, before, authoredBy, favoritedBy, withTag)` → ArticlesConnection (cursor pagination)
- `feed(first, after, last, before)` → ArticlesConnection
- `me` → User
- `profile(username: String!)` → ProfilePayload
- `tags` → [String]

**Mutations:**
- `createUser(input: CreateUserInput)` → UserResult (union: UserPayload | Error)
- `login(password, email)` → UserPayload
- `updateUser(changes: UpdateUserInput!)` → UserPayload
- `followUser(username)` / `unfollowUser(username)` → ProfilePayload
- `createArticle(input)` / `updateArticle(slug, changes)` → ArticlePayload
- `favoriteArticle(slug)` / `unfavoriteArticle(slug)` → ArticlePayload
- `deleteArticle(slug)` → DeletionStatus
- `addComment(slug, body)` → CommentPayload
- `deleteComment(slug, id)` → DeletionStatus

### Request/Response Shapes (REST)

**Registration (`POST /users`):**
```json
// Request
{ "user": { "email": "...", "username": "...", "password": "..." } }
// Response (201)
{ "user": { "email": "...", "username": "...", "token": "jwt...", "bio": "...", "image": "..." } }
```

**Login (`POST /users/login`):**
```json
// Request
{ "user": { "email": "...", "password": "..." } }
// Response (200)
{ "user": { "email": "...", "username": "...", "token": "jwt...", "bio": "...", "image": "..." } }
```

**Article (`GET /articles/{slug}`):**
```json
{
  "article": {
    "slug": "...", "title": "...", "description": "...", "body": "...",
    "tagList": ["java", "spring-boot"],
    "createdAt": "2024-01-01T00:00:00.000Z",
    "updatedAt": "2024-01-01T00:00:00.000Z",
    "favorited": false, "favoritesCount": 5,
    "author": { "username": "...", "bio": "...", "image": "...", "following": false }
  }
}
```

**Article List (`GET /articles`):**
```json
{
  "articles": [ /* array of article objects */ ],
  "articlesCount": 42
}
```

---

## 4. Business Logic Inventory

### Authentication & Authorization
- **JWT-based stateless authentication**: Tokens signed with HS512 using a secret key from `application.properties`. 24-hour expiry (`jwt.sessionTime=86400`).
- **JwtTokenFilter**: Extracts Bearer token from `Authorization` header, resolves user from DB, sets `SecurityContext`.
- **Authorization checks**: `AuthorizationService` provides static ownership checks — only article authors can update/delete their articles; comment authors or article owners can delete comments.
- **Password hashing**: BCrypt via Spring Security's `PasswordEncoder`.

### Article Management
- **Slug generation**: Titles are slugified to lowercase with special characters replaced by hyphens (`Article.toSlug()`).
- **Duplicate article detection**: Custom `@DuplicatedArticleConstraint` validator checks for existing slug before creation.
- **Tag management**: Tags are deduplicated (via `HashSet`) and reused if already present in the DB (upsert pattern in `MyBatisArticleRepository`).

### User Management
- **Registration validation**: Custom constraint validators for duplicate email (`@DuplicatedEmailConstraint`) and username (`@DuplicatedUsernameConstraint`).
- **User update validation**: `UpdateUserValidator` ensures email/username uniqueness while allowing the current user to keep their own values.
- **Default profile image**: Configured via `image.default` property.

### Social Features
- **Follow/Unfollow**: Stored as `FollowRelation` (userId → targetId) in the `follows` table.
- **Favorite/Unfavorite**: Stored as composite key in `article_favorites`.
- **Feed**: Articles from followed users, retrieved via join on `follows` table.

### CQRS Pattern
- **Write side**: `ArticleCommandService`, `UserService` handle mutations via domain repositories.
- **Read side**: `ArticleQueryService`, `CommentQueryService`, `ProfileQueryService`, `TagsQueryService` use dedicated MyBatis read services for optimized queries.
- **Extra info filling**: Article reads are enriched with favorite counts, follow status, and favorited status via batch queries.

### Pagination
- **Offset pagination**: REST endpoints use `offset`/`limit` query parameters.
- **Cursor pagination**: GraphQL endpoints use Relay-style `first`/`after`/`last`/`before` with `DateTimeCursor`.

---

## 5. Integration Points

| Integration | Technology | Details |
|-------------|-----------|---------|
| **Database** | SQLite (file: `dev.db`) | Single-file embedded DB; tests use in-memory SQLite |
| **Database Migrations** | Flyway | `V1__create_tables.sql` (schema), `V2__seed_data.sql` (seed data with 3 users, 5 articles, 7 tags) |
| **Security** | Spring Security + JWT | Stateless sessions, BCrypt passwords, `Authorization: Token <jwt>` header |
| **GraphQL** | Netflix DGS | Schema at `src/main/resources/schema/schema.graphqls`, code-generated types |
| **JSON Serialization** | Jackson | `UNWRAP_ROOT_VALUE` enabled, custom Joda `DateTime` serializer |
| **MyBatis XML Mappers** | 10 XML files in `src/main/resources/mapper/` | Separate read/write mappers following CQRS |
| **Frontend ↔ Backend** | Axios → REST API | `SERVER_BASE_URL = http://localhost:8080` hardcoded |
| **SWR** | React data fetching | Client-side caching and revalidation |
| **CORS** | Spring Security CORS config | All origins allowed (`*`), credentials disabled |

---

## 6. Build & Deployment Summary

### Backend Build
```bash
# Full build (skip spotless due to Java 17 module issue, skip JaCoCo threshold):
./gradlew build -x spotlessJava -x spotlessJavaCheck -x jacocoTestCoverageVerification --no-daemon

# Run tests:
./gradlew test -x jacocoTestCoverageVerification --no-daemon

# Run application:
./gradlew bootRun --no-daemon    # Cleans and recreates dev.db with seed data

# Docker image:
./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
```

### Frontend Build
```bash
cd frontend
npm install
NODE_OPTIONS=--openssl-legacy-provider npm run dev   # Dev server on port 3000
npm run build                                         # Production build
```

### Key Build Notes
- **Spotless** (Google Java Format) fails with Java 17 due to module access restrictions — must be skipped or run with `--add-exports` JVM args.
- **JaCoCo** coverage threshold set to 80% but actual coverage is ~33% — must be skipped with `-x jacocoTestCoverageVerification`.
- **bootRun** task deletes and recreates `dev.db` on each run to avoid Flyway migration conflicts.
- **Selenium E2E tests** are separated into their own Gradle task (`seleniumTest`) and excluded from the standard `test` task.
- **No CI/CD pipeline** is configured in the repository.
- **No Docker Compose** file exists — only `bootBuildImage` for creating a Docker image.
- **Frontend Node requirement**: Recommended Node 14–16 (specified in `.nvmrc`); `--openssl-legacy-provider` flag needed for newer Node versions.

### Test Structure
| Category | Files | Framework |
|----------|-------|-----------|
| API Controller Tests | `ArticleApiTest`, `ArticleFavoriteApiTest`, `ArticlesApiTest`, `CommentsApiTest`, `CurrentUserApiTest`, `ListArticleApiTest`, `ProfileApiTest`, `UsersApiTest` | JUnit 5 + Spring MockMvc + RestAssured |
| Query Service Tests | `ArticleQueryServiceTest`, `CommentQueryServiceTest`, `ProfileQueryServiceTest`, `TagsQueryServiceTest` | JUnit 5 + MyBatis Test |
| Domain Tests | `ArticleTest` | JUnit 5 |
| Repository Tests | `MyBatisArticleRepositoryTest`, `MyBatisCommentRepositoryTest`, `MyBatisArticleFavoriteRepositoryTest`, `MyBatisUserRepositoryTest`, `ArticleRepositoryTransactionTest` | JUnit 5 + MyBatis Test |
| Service Tests | `DefaultJwtServiceTest` | JUnit 5 |
| Selenium E2E | `SeleniumSetupTest` | TestNG + Selenium WebDriver |
