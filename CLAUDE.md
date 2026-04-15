# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Start required services (PostgreSQL + Redis)
docker-compose up -d

# Run the application
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Architecture

Spring Boot 3.5.12 / Java 21 feature flag service. Request flow for evaluation:

```
POST /evaluate → JwtAuthFilter → EvaluationController → EvaluationService
  → FlagCacheService (Redis, 5-min TTL)          [cache hit → return]
  → FlagRepository (PostgreSQL)                  [cache miss]
  → EvaluationEngine (deterministic hashing)
  → FlagCacheService.put() → EvaluationResponse
```

### Package layout

| Package | Responsibility |
|---|---|
| `auth/` | JWT generation/validation, register/login endpoints, `JwtAuthFilter` |
| `flag/` | `FeatureFlag`, `FlagVariant`, `TargetingRule` entities; CRUD service + controller |
| `evaluation/` | `EvaluationEngine` (hashing + rule matching), `EvaluationService` (orchestration + cache) |
| `cache/` | `FlagCacheService` — Redis read/write/invalidate |
| `user/` | `User` entity, `UserRole` enum (`ADMIN`/`DEVELOPER`) |
| `config/` | `SecurityConfig` (JWT filter, stateless sessions), `RedisConfig` |

### Evaluation algorithm (`EvaluationEngine`)

1. Hash `userId` → bucket 0–99 via `Math.abs(userId.hashCode()) % 100`
2. Evaluate `TargetingRule`s ordered by priority (lowest first); operators: `EQUALS`, `IN`, `NOT_IN`, `PERCENTAGE`
3. Fall through to percentage-based variant assignment
4. Disabled flags return default "off" response without hitting the engine

### Key domain rules

- Variant percentages on a flag **must sum to 100%**; `FlagService` enforces this on create/update
- `BOOLEAN` flags auto-generate two variants (`on`/`off` at 0%/100%) if none are provided
- Cache keys follow the pattern `flag:{flagKey}:user:{userId}`; `FlagService` invalidates on update, toggle, and delete
- JPA relationships use `FetchType.LAZY` with `CascadeType.ALL` + `orphanRemoval = true` on flag → variants/rules

## Infrastructure

- **PostgreSQL 16** — `localhost:5432`, db `featureflags`, user `test` / `testPassword`
- **Redis 7** — `localhost:6379`
- **Server** — `localhost:8080`
- Hibernate `ddl-auto=update` creates/updates tables automatically

## Security

All endpoints require a JWT Bearer token except `POST /auth/register`, `POST /auth/login`, `GET /health`. Tokens expire after 24 hours. Spring Security is stateless (no sessions).
