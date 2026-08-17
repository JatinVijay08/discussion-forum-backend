<div align="center">

# 🗣️ Discussion Forum — Backend

**A production-grade, real-time discussion platform built with Spring Boot 4**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Latest-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=for-the-badge&logo=websocket&logoColor=white)](https://stomp.github.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

> A full-featured backend for a Reddit-style discussion forum featuring JWT + Google OAuth2 authentication, real-time push notifications via WebSocket/STOMP, a Redis-backed intelligent feed caching system, media uploads via Cloudinary, and a pluggable Strategy Pattern feed engine (New / Hot / Trending).

</div>

---

## 📋 Table of Contents

1. [Project Overview](#-project-overview)
2. [Live Demo & Deployment](#-live-demo--deployment)
3. [Technology Stack](#-technology-stack)
4. [System Architecture](#-system-architecture)
5. [Package Structure](#-package-structure)
6. [Core Subsystems Deep-Dive](#-core-subsystems-deep-dive)
   - [Authentication System](#1-authentication-system)
   - [Feed Engine (Strategy Pattern)](#2-feed-engine--strategy-pattern)
   - [Redis Feed Caching](#3-redis-feed-caching-system)
   - [Real-Time Notification Pipeline](#4-real-time-notification-pipeline)
   - [Voting System](#5-voting-system)
   - [Media Upload (Cloudinary)](#6-media-upload--cloudinary)
7. [API Reference](#-api-reference)
8. [Data Model (ERD)](#-data-model)
9. [Security Architecture](#-security-architecture)
10. [Configuration Guide](#-configuration-guide)
11. [Running Locally](#-running-locally)
12. [Docker Deployment](#-docker-deployment)
13. [Design Decisions & Architecture Notes](#-design-decisions--architecture-notes)

---

## 🌐 Project Overview

This is the **backend service** powering a full-stack Reddit-style discussion forum. It is a stateless, REST + WebSocket API built with **Spring Boot 4** (JDK 17). The system is designed with clear separation of concerns, an intelligent caching layer, and real-time capabilities.

### Key Capabilities at a Glance

| Feature | Implementation |
|---|---|
| Authentication | JWT (HS256) + Google OAuth2 ID Token verification |
| Authorization | Stateless Spring Security filter chain |
| Feed Sorting | Strategy Pattern: `new`, `hot`, `trending` |
| Caching | Redis (TTL-based per feed type, activity-counter-driven eviction) |
| Real-Time Notifications | STOMP over WebSocket + Spring Application Events |
| Media Storage | Cloudinary (images & videos, up to 25 MB) |
| Threaded Comments | Self-referential `Comment` entity (parent-child) |
| Voting | Upvote / Downvote with toggle semantics |
| Deployment | Two-stage Docker build → single runnable JAR |

---

## 🚀 Live Demo & Deployment

| Component | URL |
|---|---|
| Frontend (Vercel) | https://discussion-forum-frontend-psi.vercel.app |
| Backend API | Configured via `PORT` environment variable (default `8080`) |
| Health Check | `GET /actuator/health` |

---

## 🛠️ Technology Stack

### Core Framework
| Technology | Version | Purpose |
|---|---|---|
| **Spring Boot** | 4.0.1 | Application framework & auto-configuration |
| **Java** | 17 | Language (LTS, records, pattern matching) |
| **Spring Web MVC** | (Boot 4.x) | REST controllers, request mapping, validation |
| **Spring Data JPA** | (Boot 4.x) | ORM layer, repository abstraction |
| **Hibernate** | (Boot 4.x) | JPA provider, DDL auto-update |
| **Spring Security** | (Boot 4.x) | Authentication, authorization, CORS, CSRF |

### Data & Messaging
| Technology | Version | Purpose |
|---|---|---|
| **PostgreSQL** | Latest | Primary relational database |
| **Redis** | Latest | Feed caching + activity counters |
| **HikariCP** | (Boot 4.x) | Database connection pool (configured for cloud environments) |
| **Spring WebSocket** | (Boot 4.x) | STOMP-based WebSocket broker |

### Security & Auth
| Technology | Version | Purpose |
|---|---|---|
| **JJWT (io.jsonwebtoken)** | 0.11.5 | JWT generation, signing (HS256), validation |
| **Spring OAuth2 Client** | (Boot 4.x) | Google OAuth2 dependency chain |
| **BCryptPasswordEncoder** | — | Password hashing (strength 10) |
| **Apache HttpClient5** | 5.2.1 | Google token verification HTTP calls |

### Infrastructure & Tooling
| Technology | Version | Purpose |
|---|---|---|
| **Cloudinary** | 2.3.2 | Cloud media storage (images & video) |
| **Lombok** | Latest | Boilerplate reduction (`@Builder`, `@Slf4j`, etc.) |
| **Jackson JSR-310** | (Boot 4.x) | `Instant` / Java Time serialization |
| **Spring Actuator** | (Boot 4.x) | Health endpoint for uptime monitors |
| **Maven** | 3.9.6 | Build & dependency management |
| **Docker** | — | Two-stage containerisation |

---

## 🏗️ System Architecture

### High-Level Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                     │
│   Browser / Mobile App (React @ Vercel)                                       │
│                                                                               │
│   ┌──────────────┐   REST/JSON   ┌──────────────────┐   STOMP/WS             │
│   │  HTTP Client │ ───────────► │  Spring Boot API  │ ◄──────── WebSocket    │
│   └──────────────┘              │   (Port 8080)     │                        │
└─────────────────────────────────┼──────────────────┼────────────────────────┘
                                  │                  │
          ┌───────────────────────┼──────────────────┼───────────────────┐
          │         SPRING BOOT APPLICATION           │                   │
          │                       │                  │                   │
          │  ┌────────────────────▼──────────────┐   │                   │
          │  │      SECURITY FILTER CHAIN         │   │                   │
          │  │  JwtAuthFilter → SecurityContext  │   │                   │
          │  │  JwtChannelInterceptor (STOMP)    │   │                   │
          │  └────────────────────┬──────────────┘   │                   │
          │                       │                  │                   │
          │  ┌────────────────────▼──────────────┐   │                   │
          │  │         CONTROLLER LAYER           │   │                   │
          │  │  AuthController   PostController   │   │                   │
          │  │  CommentController UserController  │   │                   │
          │  │  NotificationController            │   │                   │
          │  └────────────────────┬──────────────┘   │                   │
          │                       │                  │                   │
          │  ┌────────────────────▼──────────────┐   │                   │
          │  │          SERVICE LAYER             │   │                   │
          │  │  AuthService    PostService        │   │                   │
          │  │  CommentService VoteService        │   │                   │
          │  │  NotificationService               │   │                   │
          │  │  FeedCacheService  PostMapper      │   │                   │
          │  └──────┬─────────────────────────────┘   │                   │
          │         │                                  │                   │
          │  ┌──────▼──────────┐   ┌──────────────────▼───────────────┐  │
          │  │  FEED STRATEGY  │   │     NOTIFICATION PIPELINE        │  │
          │  │  ┌────────────┐ │   │  NotificationService             │  │
          │  │  │ NewFeed    │ │   │    └─ ApplicationEventPublisher  │  │
          │  │  │ HotFeed    │ │   │  NotificationEventListener       │  │
          │  │  │ Trending   │ │   │    └─ @TransactionalEventListener│  │
          │  │  └────────────┘ │   │  NotificationRealtimeService     │  │
          │  └──────┬──────────┘   │    └─ SimpMessagingTemplate      │  │
          │         │              └──────────────────────────────────┘  │
          │  ┌──────▼──────────────────────────────────────────────────┐ │
          │  │                REPOSITORY LAYER (Spring Data JPA)        │ │
          │  │  PostRepo  CommentRepo  UserRepo  NotificationRepo        │ │
          │  │  PostVoteRepo  CommentVoteRepo                           │ │
          │  └──────┬──────────────────────────────────────────────────┘ │
          └─────────┼────────────────────────────────────────────────────┘
                    │
       ┌────────────┼─────────────────────────┐
       │            │                         │
  ┌────▼──────┐  ┌──▼──────────┐  ┌──────────▼───┐
  │ PostgreSQL │  │    Redis    │  │  Cloudinary  │
  │  (Primary) │  │  (Cache)   │  │   (Media)    │
  └────────────┘  └────────────┘  └──────────────┘
```

---

### Request Lifecycle Flow

```
┌──────────┐   HTTP Request   ┌──────────────────────────────────────────────┐
│  Client  │ ───────────────► │  Spring Security Filter Chain                │
└──────────┘                  │                                              │
                              │  1. CorsFilter (whitelist origins)           │
                              │  2. JwtAuthenticationFilter                  │
                              │     ├── Extract "Bearer <token>" header      │
                              │     ├── jwtUtil.isValid(token)               │
                              │     ├── Extract email from claims            │
                              │     └── Set UsernamePasswordAuthenticationToken│
                              │         in SecurityContextHolder             │
                              │  3. AuthorizationFilter (role/auth checks)   │
                              └───────────────────┬──────────────────────────┘
                                                  │
                              ┌───────────────────▼──────────────────────────┐
                              │  @RestController (e.g. PostController)        │
                              │  Route matched, params bound & validated      │
                              └───────────────────┬──────────────────────────┘
                                                  │
                              ┌───────────────────▼──────────────────────────┐
                              │  @Service (e.g. PostService)                  │
                              │  Business logic + transaction boundary        │
                              └───────────────────┬──────────────────────────┘
                                                  │
                              ┌───────────────────▼──────────────────────────┐
                              │  @Repository (Spring Data JPA)                │
                              │  JPQL queries → Hibernate → PostgreSQL       │
                              └───────────────────┬──────────────────────────┘
                                                  │
                              ┌───────────────────▼──────────────────────────┐
                              │  @RestControllerAdvice (GlobalExceptionHandler)│
                              │  Structured ApiError JSON on any exception    │
                              └──────────────────────────────────────────────┘
```

---

## 📁 Package Structure

```
com.jatin.forum/
│
├── config/                      ← Spring @Configuration classes
│   ├── CacheConfig.java         ← Redis template, JSON serialisation
│   ├── CloudinaryConfig.java    ← Cloudinary SDK bean
│   ├── SecurityConfig.java      ← Filter chain, CORS, BCrypt bean
│   └── WebSocketConfig.java     ← STOMP endpoints, broker, JWT interceptor
│
├── controller/                  ← REST endpoints (@RestController)
│   ├── AuthController.java      ← /api/auth/**
│   ├── PostController.java      ← /api/posts/**
│   ├── CommentController.java   ← /api/comments/**
│   ├── NotificationController.java ← /api/notifications/**
│   ├── UserController.java      ← /api/users/**
│   └── HealthController.java    ← /actuator/health
│
├── dto/                         ← Data Transfer Objects (records & classes)
│   ├── ApiError.java            ← Standardised error response
│   ├── CachedFeed.java          ← Redis-serialisable feed snapshot
│   ├── CachedPost.java          ← Lightweight cached post projection
│   ├── PostResponse.java        ← Full post response (includes voteType)
│   ├── PostFeedResponse.java    ← Paginated feed (posts + cursor + hasMore)
│   ├── NotificationCreatedEvent.java ← Internal Spring event DTO
│   ├── NotificationResponse.java    ← WebSocket push payload
│   └── ...                      ← Other request/response DTOs
│
├── entity/                      ← JPA @Entity classes
│   ├── User.java                ← Users table (LOCAL / GOOGLE auth)
│   ├── Post.java                ← Posts (with denormalised vote/comment counts)
│   ├── Comment.java             ← Self-referential (parentComment)
│   ├── Notification.java        ← Persistent notification records
│   ├── PostVote.java / CommentVote.java ← Vote join entities
│   ├── AuthProvider.java        ← Enum: LOCAL | GOOGLE
│   ├── NotificationType.java    ← Enum: POST_LIKE | POST_COMMENT | COMMENT_REPLY
│   └── VoteType.java            ← Enum: upvote | downvote
│
├── eventListeners/
│   └── NotificationEventListener.java ← @TransactionalEventListener (AFTER_COMMIT)
│
├── exception/                   ← Custom exceptions + global handler
│   ├── GlobalExceptionHandler.java
│   ├── InvalidCredentialsException.java
│   ├── ResourceNotFoundException.java
│   └── UserAlreadyExistsException.java
│
├── repository/                  ← Spring Data JPA interfaces
│   ├── PostRepo.java            ← Custom JPQL: findPostNew, findPostRecent,
│   │                               increment/decrementCounts
│   ├── NotificationRepo.java    ← Cursor-based JPQL fetch + bulk mark-read
│   └── ...
│
├── security/
│   ├── JwtAuthenticationFilter.java  ← OncePerRequestFilter for REST
│   ├── JwtChannelInterceptor.java    ← STOMP CONNECT frame JWT validation
│   └── CustomAuthenticationEntryPoint.java ← 401 JSON responses
│
├── service/                     ← Business logic
│   ├── AuthService.java         ← register / login / googleLogin
│   ├── PostService.java         ← CRUD + feed delegation to strategies
│   ├── CommentService.java      ← Threaded comments + vote counting
│   ├── VoteService.java         ← Toggle vote logic + cache eviction
│   ├── NotificationService.java ← Persistence + event publishing
│   ├── NotificationRealtimeService.java ← SimpMessagingTemplate wrapper
│   ├── FeedCacheService.java    ← Redis get/set/evict + activity counters
│   ├── PostMapper.java          ← Hot score / trending score + DTO mapping
│   ├── CurrentUserService.java  ← SecurityContext → User entity lookup
│   ├── CloudinaryService.java   ← Media upload facade
│   └── GoogleTokenVerifierService.java ← Google ID token HTTP verification
│
├── strategy/                    ← Feed Strategy Pattern
│   ├── FeedStrategy.java        ← Interface: fetchFeed(user, page, limit, cursor)
│   ├── NewFeedStrategy.java     ← @Component("new") cursor-based pagination
│   ├── HotFeedStrategy.java     ← @Component("hot") time-decay score
│   └── TrendingFeedStrategy.java ← @Component("trending") recent engagement
│
└── utilities/
    └── JwtUtil.java             ← Token generation (HS256) + validation
```

---

## 🔬 Core Subsystems Deep-Dive

### 1. Authentication System

The API supports **two authentication providers** that both result in the same JWT token being issued, ensuring a unified auth flow downstream.

#### Flow A — Email/Password (LOCAL)

```
Client                     AuthController               AuthService              PostgreSQL
  │                              │                           │                       │
  │── POST /api/auth/register ──►│                           │                       │
  │   { email, username, pass }  │── register(req) ─────────►│                       │
  │                              │                           │── findByEmail? ───────►│
  │                              │                           │◄── null (not exists) ──│
  │                              │                           │── BCrypt.encode(pass) ─│
  │                              │                           │── userRepo.save() ────►│
  │◄── 201 "Registered" ─────────│◄──────────────────────────│                       │
  │                              │                           │                       │
  │── POST /api/auth/login ─────►│                           │                       │
  │   { email, password }        │── login(req) ────────────►│                       │
  │                              │                           │── findByEmail ────────►│
  │                              │                           │◄── User entity ────────│
  │                              │                           │── BCrypt.matches() ───►│
  │                              │                           │── jwtUtil.generate() ──│
  │                              │                           │── save(lastLoginAt) ──►│
  │◄── { token, username } ──────│◄── LoginResponseDto ───────│                       │
```

#### Flow B — Google OAuth2 (GOOGLE)

```
Client                 AuthController          GoogleTokenVerifierService      AuthService
  │                         │                           │                         │
  │─ POST /api/auth/google ►│                           │                         │
  │  { idToken: "eyJ..." }  │── googleLogin(token) ─────────────────────────────►│
  │                         │                           │◄── HTTP GET tokeninfo ──│
  │                         │                           │    (Google API)         │
  │                         │                           │── verify token fields ──│
  │                         │                           │── return { email, name, sub }
  │                         │                           │                         │
  │                         │                           │   if user exists (GOOGLE):
  │                         │                           │     issue JWT            │
  │                         │                           │   if user exists (LOCAL):
  │                         │                           │     throw conflict error │
  │                         │                           │   if new user:           │
  │                         │                           │     generateUsername()   │
  │                         │                           │     create + save User   │
  │                         │                           │     issue JWT            │
  │◄── { token, username } ─│◄────────────────────────────────────────────────────│
```

**Username generation for Google users:** `name.toLowerCase().replaceAll("\\s+","_")` with collision suffix `_1`, `_2`, etc.

---

### 2. Feed Engine — Strategy Pattern

The feed system uses the **Gang of Four Strategy Pattern** to decouple sorting algorithms from the `PostService`. The strategy is resolved at runtime from a Spring-injected `Map<String, FeedStrategy>` where the map key is the `@Component` bean name.

```
PostController
  └── GET /api/posts?sort=hot&page=0&limit=10
        │
        ▼
  PostService.getAllPosts(sort="hot", page=0, limit=10, cursor=null)
        │
        ├── feedStrategyMap.get("hot")  ──► HotFeedStrategy
        ├── feedStrategyMap.get("new")  ──► NewFeedStrategy
        └── feedStrategyMap.get("trending") ──► TrendingFeedStrategy
              │
              ▼
        strategy.fetchFeed(user, page, limit, cursor)
```

#### Feed Strategy Comparison

| Strategy | Algorithm | Time Window | Scoring Formula | Caching |
|---|---|---|---|---|
| **New** | Cursor-based time-ordered | All time | `createdAt DESC` | First page only (cursor=null) |
| **Hot** | Score-ranked time-decay | Last 7 days | `votes / (age_hours + 2)^1.5` | Page 0 only |
| **Trending** | Recent engagement score | Last 24 hours | `recent_votes + (recent_comments × 2)` | Page 0 only |

#### Hot Score Algorithm

The `PostMapper.getHotScoreFromValue()` implements a **time-decay ranking** inspired by Hacker News:

```
              votes
score = ─────────────────
         (hours_old + 2)^1.5
```

- Fresh posts with few votes can outrank old posts with many votes.
- The `+2` prevents division by zero for brand-new posts.
- `1.5` exponent controls how fast old posts decay.

#### Trending Score Algorithm

```
trending_score = recent_votes_6h + (recent_comments_6h × 2)
```

- Comments are weighted **2×** votes because commenting requires more intent.
- The 6-hour window ensures only currently active posts trend.

---

### 3. Redis Feed Caching System

The `FeedCacheService` implements a **smart, activity-counter-driven cache invalidation** strategy.

#### Cache Keys

```
feed:{type}:size:{limit}        ← stores CachedFeed (posts + hasMore + cursor)
feed:{type}:activity            ← counter: how many writes happened since last cache
```

Example keys: `feed:new:size:10`, `feed:hot:activity`

#### Eviction Policy per Feed Type

| Feed | Activity Threshold | Meaning |
|---|---|---|
| **new** | 1 write | Evict immediately on any new post (new feed must always be fresh) |
| **hot** | 10 writes | Evict after 10 votes/posts (hot changes slowly) |
| **trending** | 10 writes | Evict after 10 interactions (votes + comments) |

#### Cache Flow — Cache Hit

```
Request → FeedCacheService.getCachedFeed("new", 10)
              │
              ▼
        Redis GET "feed:new:size:10"
              │
        ┌─────┴──────┐
        │  HIT        │                   MISS
        │  CachedFeed │────────────────────────────────────────────────────►
        │  returned   │                                                      │
        └─────────────┘                                              Hit DB  │
                                                                     Store in Redis
                                                                     Return response
```

#### Cache Invalidation on Write

```
User creates a post
  │
  ├── feedCacheService.incrementActivity("new")   → count = 1
  ├── shouldEvict("new", 1) → TRUE (threshold=1)
  ├── evictFeed("new", 10)  → Redis DELETE "feed:new:size:10"
  └── resetActivity("new")  → Redis DELETE "feed:new:activity"
  
  ├── feedCacheService.incrementActivity("hot")
  └── feedCacheService.incrementActivity("trending")
      (no eviction yet — thresholds not met)
```

#### TTL Configuration
- **Feed cache TTL:** 2 minutes (`FEED_TTL`)
- **Activity counter TTL:** 10 minutes (`ACTIVITY_TTL`) — auto-expires stale counters

---

### 4. Real-Time Notification Pipeline

The notification system uses **Spring's Application Event mechanism** to decouple the notification creation (which happens inside a `@Transactional` boundary) from the WebSocket push (which must happen AFTER commit).

#### Architecture

```
VoteService / CommentService
  │
  │  @Transactional boundary begins
  │
  ├── notificationService.createNotification(post, user, POST_LIKE)
  │     │
  │     ├── Guard: no self-notifications (creator ≠ post owner)
  │     ├── notificationRepo.save(notification)  ← persisted to DB
  │     └── applicationEventPublisher.publishEvent(NotificationCreatedEvent)
  │                                               ← event queued, NOT yet sent
  │
  │  @Transactional boundary COMMITS
  │
  ▼
NotificationEventListener
  │  @TransactionalEventListener(phase = AFTER_COMMIT)
  │  Fires ONLY after successful DB commit
  │
  └── notificationRealtimeService.sendNotification(email, notificationResponse)
          │
          └── SimpMessagingTemplate.convertAndSendToUser(
                  email,                    ← Spring STOMP user destination
                  "/queue/notifications",   ← private queue
                  notificationResponse      ← JSON payload
              )
```

**Why `AFTER_COMMIT`?** If the transaction rolls back (e.g., a DB error), the WebSocket push would be silently discarded, preventing ghost notifications for events that never actually happened.

#### WebSocket Security — JwtChannelInterceptor

Standard Spring Security filter chains do not apply to WebSocket upgrade requests. A dedicated `JwtChannelInterceptor` is registered on the **inbound channel** and intercepts every STOMP `CONNECT` frame:

```
STOMP CONNECT frame
  │  Headers: { Authorization: "Bearer eyJ..." }
  │
  ▼
JwtChannelInterceptor.preSend()
  ├── Reject if no Authorization header
  ├── jwtUtil.isValid(token)
  ├── Extract email → look up User in DB
  ├── Create UsernamePasswordAuthenticationToken
  └── accessor.setUser(authentication)
        │
        └── Spring now routes "/user/queue/notifications"
            to the correct authenticated socket session
```

#### Notification Types

| Type | Trigger | Recipient |
|---|---|---|
| `POST_LIKE` | User upvotes a post | Post author |
| `POST_COMMENT` | User comments on a post | Post author |
| `COMMENT_REPLY` | User replies to a comment | Original comment author |

All notifications support **read/unread state** and are persisted to PostgreSQL for historical fetch.

---

### 5. Voting System

The voting system implements **idempotent toggle semantics**:

```
State Machine for a single (user, post) pair:
  
  No Vote ──── upvote ────► UPVOTED
     ▲                          │
     │                 same     │  switch
     └──────────────── upvote ──┤  to downvote
                                │
  No Vote ──── downvote ──► DOWNVOTED
     ▲                          │
     └──────────────── downvote─┘
```

| Current State | Action | Result |
|---|---|---|
| No vote | upvote | Create PostVote(UP), increment upvotesCount |
| No vote | downvote | Create PostVote(DOWN), increment downvotesCount |
| Upvoted | upvote again | Delete PostVote, decrement upvotesCount (**toggle off**) |
| Upvoted | downvote | Update PostVote(DOWN), inc downvotes + dec upvotes |
| Downvoted | downvote again | Delete PostVote, decrement downvotesCount (**toggle off**) |

**Denormalised counters:** `upvotesCount` and `downvotesCount` are stored directly on the `Post` entity (no re-aggregation query on read). They are updated via `@Modifying @Query` JPQL — atomic, single-row updates. The `net_votes` returned to clients is always computed at response time: `upvotes - downvotes`.

---

### 6. Media Upload — Cloudinary

```
POST /api/posts (multipart/form-data)
  │  Parts: title, content, media (optional file)
  │
  ▼
PostController.createPost()
  │
  ├── If media file present:
  │     cloudinaryService.upload(media)
  │     └── Returns { secure_url, public_id, resource_type }
  │
  └── postService.createPost(title, content, mediaUrl, mediaType, mediaPublicId)
          └── Stores all three Cloudinary fields on Post entity
```

- **Max file size:** 25 MB (configured in `application.properties`)
- **Supported types:** Images and videos (`resource_type` = `"image"` or `"video"`)
- `mediaPublicId` is stored for **future deletion** from Cloudinary when a post is deleted.

---

## 📡 API Reference

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register with email + password |
| `POST` | `/api/auth/login` | Public | Email/password login → JWT |
| `POST` | `/api/auth/google` | Public | Google OAuth2 ID token → JWT |

### Posts

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/posts` | Optional | Feed: `?sort=new\|hot\|trending&page=0&limit=10&cursor=` |
| `POST` | `/api/posts` | Required | Create post (multipart: title, content, media?) |
| `GET` | `/api/posts/{id}` | Optional | Get single post |
| `DELETE` | `/api/posts/{id}` | Required (owner) | Delete post |
| `POST` | `/api/posts/{postId}/votes` | Required | Vote: `{ "voteType": "upvote"\|"downvote" }` |

### Comments

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/comments/post/{postId}` | Optional | Paginated comments: `?page=0&size=10` |
| `POST` | `/api/comments/post/{postId}` | Required | Create comment: `{ content, parentId? }` |
| `DELETE` | `/api/comments/{commentId}` | Required (owner) | Delete comment |
| `POST` | `/api/comments/{commentId}/votes` | Required | Vote on comment |

### Users

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users/recent` | Public | Recent active users (last 5) |
| `GET` | `/api/users/{username}` | Optional | User profile |
| `PATCH` | `/api/users/{id}/username` | Required (self) | Update username |

### Notifications

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/notifications` | Required | Cursor-paginated: `?limit=10&cursor=` |
| `GET` | `/api/notifications/unread-count` | Required | Count of unread notifications |
| `PATCH` | `/api/notifications/read-all` | Required | Mark all as read |

### WebSocket

| Endpoint | Type | Description |
|---|---|---|
| `/ws` | STOMP Upgrade | Connect with `Authorization: Bearer <token>` header |
| `/user/queue/notifications` | Subscribe | Receive real-time notification pushes |

### Standardised Error Response

All errors return a consistent `ApiError` JSON:

```json
{
  "timestamp": "2026-08-17T11:15:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Please login to perform this action",
  "path": "/api/posts",
  "fieldErrors": null
}
```

---

## 🗄️ Data Model

```
┌─────────────────────────┐     ┌──────────────────────────────┐
│         users           │     │            posts             │
├─────────────────────────┤     ├──────────────────────────────┤
│ id (PK)                 │────►│ id (PK)                      │
│ username (UNIQUE)       │     │ title                        │
│ email (UNIQUE)          │     │ content (TEXT)               │
│ password (nullable)     │     │ created_at                   │
│ auth_provider           │     │ media_url                    │
│ google_id               │     │ media_type                   │
│ created                 │     │ media_public_id              │
│ last_login_at           │     │ user_id (FK → users)         │
└─────────────────────────┘     │ comment_count ← denormalised │
                                │ upvotes_count ← denormalised │
                                │ downvotes_count ← denormalised│
                                └──────────────────────────────┘
                                          │
          ┌───────────────────────────────┤
          │                               │
          ▼                               ▼
┌──────────────────────┐     ┌──────────────────────────────┐
│      post_votes      │     │          comments            │
├──────────────────────┤     ├──────────────────────────────┤
│ id (PK)              │     │ id (PK)                      │
│ user_id (FK)         │     │ content                      │
│ post_id (FK)         │     │ created_at                   │
│ vote_type            │     │ user_id (FK → users)         │
│ created_at           │     │ post_id (FK → posts)         │
└──────────────────────┘     │ parent_comment_id (FK → self)│
                             │ upvotes ← denormalised       │
                             │ downvotes ← denormalised     │
                             └──────────────────────────────┘
                                          │
                             ┌────────────┘
                             ▼
                  ┌──────────────────────┐     ┌──────────────────────────────┐
                  │    comment_votes     │     │        notification          │
                  ├──────────────────────┤     ├──────────────────────────────┤
                  │ id (PK)              │     │ id (PK)                      │
                  │ user_id (FK)         │     │ creator_id (FK → users)      │
                  │ comment_id (FK)      │     │ receiver_id (FK → users)     │
                  │ vote_type            │     │ type (enum)                  │
                  └──────────────────────┘     │ post_id (nullable FK)        │
                                               │ comment_id (nullable FK)     │
                                               │ read (boolean, default false)│
                                               │ created_at                   │
                                               └──────────────────────────────┘
```

---

## 🔐 Security Architecture

```
                      ┌────────────────────────────────────┐
                      │      PUBLIC ENDPOINTS              │
                      │  POST  /api/auth/**                │
                      │  GET   /api/posts                  │
                      │  GET   /api/posts/{id}             │
                      │  GET   /api/comments/post/{id}     │
                      │  GET   /api/users/recent           │
                      │  GET   /actuator/health            │
                      │  WS    /ws/**                      │
                      └────────────────────────────────────┘

                      ┌────────────────────────────────────┐
                      │    AUTHENTICATED ENDPOINTS         │
                      │  POST /api/posts (create)          │
                      │  DELETE /api/posts/{id} (owner)    │
                      │  POST /api/posts/{id}/votes        │
                      │  POST /api/comments/**             │
                      │  GET  /api/notifications/**        │
                      │  PATCH /api/users/**               │
                      └────────────────────────────────────┘

Security Design Choices:
  ✅ Stateless JWT — no server-side sessions
  ✅ SessionCreationPolicy.STATELESS
  ✅ CSRF disabled (stateless APIs are not CSRF-vulnerable)
  ✅ HTTP Basic / Form login disabled
  ✅ Custom CustomAuthenticationEntryPoint → 401 JSON (not redirect)
  ✅ CORS: whitelist [localhost:5173, Vercel production URL]
  ✅ BCryptPasswordEncoder for password hashing
  ✅ JWT signed with HS256 (configurable secret, min 32 bytes)
  ✅ JWT expiry: 1 hour (short-lived, must re-login)
  ✅ WebSocket STOMP connections validated via JwtChannelInterceptor
```

---

## ⚙️ Configuration Guide

### Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `DATABASE_URL` | ✅ | `jdbc:postgresql://localhost:5432/forum_db` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | ✅ | `postgres` | DB username |
| `DATABASE_PASSWORD` | ✅ | `postgres` | DB password |
| `JWT_SECRET` | ✅ | `dev_secret_key_...` | HS256 signing secret (≥ 32 bytes) |
| `GOOGLE_CLIENT_ID` | ✅ | — | Google OAuth2 client ID |
| `CLOUDINARY_CLOUD_NAME` | ✅ | — | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | ✅ | — | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | ✅ | — | Cloudinary API secret |
| `REDIS_HOST` | ✅ | `localhost` | Redis host |
| `REDIS_PORT` | ✅ | `6379` | Redis port |
| `REDIS_PASSWORD` | ⬜ | `` (empty) | Redis password (if any) |
| `PORT` | ⬜ | `8080` | Server listen port |

### HikariCP Connection Pool (Tuned for Cloud)

```properties
# Configured for low-connection cloud DB tiers (e.g. Render, Railway)
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=0
spring.datasource.hikari.idle-timeout=60000        # 1 min
spring.datasource.hikari.max-lifetime=240000       # 4 min
spring.datasource.hikari.keepalive-time=45000      # 45 sec
spring.datasource.hikari.leak-detection-threshold=10000
```

This conservative pool configuration prevents connection exhaustion on cloud database providers with small connection limits.

---

## 🖥️ Running Locally

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL (running, with a `forum_db` database)
- Redis (running on localhost:6379)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/JatinVijay08/discussion-forum-backend.git
cd discussion-forum-backend

# 2. Create a local properties file (never committed)
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties

# 3. Fill in your environment-specific values in application-local.properties

# 4. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# OR using the Maven wrapper on Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

The API will be available at `http://localhost:8080`.

---

## 🐳 Docker Deployment

The project uses a **two-stage Docker build** to keep the final image minimal:

```dockerfile
# Stage 1: Build (Maven + JDK 17)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B      # cache dependencies layer
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (JRE only, no build tools)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
```

```bash
# Build the image
docker build -t discussion-forum-backend .

# Run with environment variables
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/forum_db \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=your_32_byte_minimum_secret_here \
  -e GOOGLE_CLIENT_ID=your_client_id \
  -e CLOUDINARY_CLOUD_NAME=your_cloud \
  -e CLOUDINARY_API_KEY=your_key \
  -e CLOUDINARY_API_SECRET=your_secret \
  -e REDIS_HOST=host.docker.internal \
  discussion-forum-backend
```

---

## 🧠 Design Decisions & Architecture Notes

### 1. Strategy Pattern for Feed Sorting
Rather than a single `PostService` method with a growing `if/else` chain for sort modes, each feed algorithm is encapsulated as a Spring `@Component` implementing `FeedStrategy`. `PostService` receives a `Map<String, FeedStrategy>` injected by Spring, and resolves the correct strategy at runtime via `map.get(sort)`. Adding a new sort mode (e.g., `"controversial"`) requires zero changes to existing code — just a new class annotated `@Component("controversial")`.

### 2. Denormalised Vote & Comment Counts on Post Entity
Rather than querying `COUNT(*)` from `post_votes` or `comments` tables on every feed load, `upvotesCount`, `downvotesCount`, and `commentCount` are stored directly on `Post`. They are maintained via atomic `@Modifying @Query` JPQL updates (`UPDATE Post p SET p.upvotesCount = p.upvotesCount + 1 WHERE p.id = :id`). This dramatically reduces feed read complexity from O(N queries) to O(1).

### 3. Transactional Event Listener for WebSocket Push
`@TransactionalEventListener(phase = AFTER_COMMIT)` ensures that a WebSocket push is fired only after the notification record is durably persisted in PostgreSQL. If the transaction rolls back (e.g., DB error), the push is silently cancelled — avoiding ghost notifications that point to non-existent records.

### 4. Cursor-Based Pagination for New Feed
The `new` feed uses **cursor-based pagination** (using the post's `createdAt` timestamp) rather than offset-based `LIMIT`/`OFFSET` pagination. This avoids the classic "duplicate / missing posts on scroll" problem caused by new posts shifting offset windows during pagination, and is more efficient at scale.

### 5. Redis Cache Architecture — Activity Counter Approach
Instead of a simple TTL-only expiry, each feed type tracks a write activity counter in Redis. When writes accumulate past a threshold (`hot`/`trending`: 10, `new`: 1), the cache is pro-actively evicted. This balances freshness with database load: `hot` feed doesn't evict on every single vote, but `new` feed always evicts on any new post.

### 6. JWT in WebSocket STOMP Headers
Standard HTTP Authorization headers are not sent on WebSocket upgrade requests by browsers. The solution is to send the JWT as a STOMP `CONNECT` frame header (`Authorization: Bearer <token>`), intercepted by `JwtChannelInterceptor`. This is a well-known pattern for secured WebSocket APIs.

### 7. Lazy Initialization for Startup Performance
`spring.main.lazy-initialization=true` is enabled in `application.properties`. Beans are not initialised until first use, significantly reducing cold-start time — especially important on cloud platforms with memory/cold-start constraints (e.g., Render free tier).

### 8. Batch Vote Query via IN Clause
When loading a feed page, rather than querying `PostVote` N times (once per post) to find each user's vote state, a single query `WHERE user = :user AND post_id IN (:ids)` is issued. The results are loaded into a `HashMap<Long, VoteType>` and looked up O(1) per post during DTO mapping in `PostMapper`.

### 9. Google OAuth2 Without Spring's Built-in Provider
Rather than using Spring Security's full OAuth2 login flow (which requires browser redirects), the backend validates a **Google ID Token** sent directly from the frontend. `GoogleTokenVerifierService` makes a synchronous HTTP GET to `https://oauth2.googleapis.com/tokeninfo?id_token=...` using Apache HttpClient5, extracts the claims, and proceeds to issue a JWT. This is intentional for a **headless API design** (no server-side redirects needed).

### 10. Two-Stage Docker Build
The Dockerfile separates the Maven build stage from the runtime stage. The final image contains only the JRE (not a full JDK or Maven installation), keeping it small and secure. The `dependency:go-offline` step creates a cached Docker layer, so subsequent builds only rebuild when `pom.xml` changes.

---

## 📊 Performance Characteristics

| Scenario | Expected Behaviour |
|---|---|
| Feed load (cache hit) | Redis O(1) read + 1 batch DB vote query |
| Feed load (cache miss) | 1–2 PostgreSQL queries + Redis write |
| Vote on post | 1 DB read + 1–2 atomic JPQL updates + Redis counter increment |
| Notification push | DB save + Spring event + WebSocket frame (all within one request) |
| Google login | 1 external HTTP call (Google) + 1 DB read/write + JWT issue |

---

<div align="center">

Built with ❤️ by **Jatin** · Spring Boot 4 · PostgreSQL · Redis · WebSocket

</div>
