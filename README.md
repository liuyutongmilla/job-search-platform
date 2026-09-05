# AI Job Search Platform

An AI-powered job search platform built with **Java 21, Spring Boot 4, Spring Cloud 2025.1.0, PostgreSQL, Redis, and Apache Kafka**.

The project follows a microservices architecture that separates user management, job management, job matching, notifications, configuration, API routing, and service discovery into independent Spring Boot applications.
Business services follow a database-per-service persistence model.

> **Current architecture:** Service discovery is provided by Netflix Eureka with client-side load balancing through Spring Cloud LoadBalancer.

The platform is designed as a modular backend system with independently deployable services, isolated data ownership, synchronous service communication, asynchronous event processing, and centralized infrastructure configuration.

---

## Architecture

```text
                         Client
                            |
                            v
                    +---------------+
                    |  API Gateway  |
                    |     :8072     |
                    +-------+-------+
                            |
             +--------------+--------------+
             |              |              |
             v              v              v
      +-------------+ +-------------+ +------------------+
      | User Service| | Job Service | | Matching Service |
      |    :8080    | |    :8090    | |      :9000       |
      +-------------+ +-------------+ +--------+---------+
             |              |                  |
      userdb              jobdb            matchingdb
    (PostgreSQL)        (PostgreSQL)       (PostgreSQL)
                                                |
                                                v
                                               Redis
                                           (matching cache)

      API Gateway --------------------------------> Redis
                                             (rate limiting)


                  Matching Service
                         |
                         | match-computed event
                         v
                       Kafka
                       :9092
                         |
                         v
                +----------------------+
                | Notification Service |
                |        :9010         |
                +----------------------+


                +----------------------+       +----------------------+
                |    Config Server     |       |    Eureka Server     |
                |        :8071         |       |        :8761         |
                +----------------------+       +----------------------+
                            
```

Each business service owns an isolated PostgreSQL database (`userdb`, `jobdb`, `matchingdb`) — services never share tables directly and only interact through APIs (Feign) or events (Kafka). This preserves the ability to deploy, scale, or migrate each service independently.

- `user-service` → `userdb`
- `job-service` → `jobdb`
- `matching-service` → `matchingdb`

Services do not access another service's tables directly. Cross-service data is exchanged through REST APIs with OpenFeign or through Kafka events, preserving independent service boundaries.

---

## Key Capabilities

- User account and resume management
- Job creation, retrieval, update, deletion, and search
- Candidate-to-job matching workflow
- Database-per-service persistence model
- Unified external API routing through Spring Cloud Gateway
- Dynamic service discovery through Netflix Eureka
- Client-side load balancing with Spring Cloud LoadBalancer
- Synchronous service-to-service communication with OpenFeign
- Fault-tolerant remote communication with Resilience4j
- Redis-backed API rate limiting and matching cache
- Event-driven notification processing with Apache Kafka
- Centralized configuration through Spring Cloud Config
- Database schema management with Flyway
- Containerized local infrastructure with Docker Compose


---

## Microservices

| Service                | Port | Responsibility                                               |
| ---------------------- | ---: | ------------------------------------------------------------ |
| `gatewayserver`        | 8072 | API routing, rate limiting, circuit breaking, request tracing |
| `configserver`         | 8071 | Centralized configuration management (native profile, encryptable properties) |
| `eurekaserver`         | 8761 | Service registration and discovery                           |
| `user-service`         | 8080 | User accounts and resume upload/download                     |
| `job-service`          | 8090 | Job postings, search, and lifecycle management               |
| `matching-service`     | 9000 | Job matching workflow, orchestrates user/job/AI lookups, publishes match events |
| `notification-service` | 9010 | Asynchronous email/SMS notifications consumed from Kafka     |

---


## Tech Stack

### Backend

- Java 21
- Spring Boot 4
- Spring MVC / REST APIs
- Spring Data JPA
- OpenFeign
- Springdoc OpenAPI (Swagger UI on `user-service`, `job-service`, `matching-service`)

### Microservices

- Spring Cloud Gateway (WebFlux)
- Spring Cloud Config
- Netflix Eureka
- Spring Cloud LoadBalancer
- Resilience4j (circuit breaker, rate limiter, retry)

### Data & Messaging

- PostgreSQL (one database per business service)
- Redis (gateway rate limiting, matching cache)
- Apache Kafka (KRaft mode, no ZooKeeper) + Kafka UI
- Spring Cloud Stream (functional `Consumer<T>` bindings in `notification-service`)
- Flyway

### Infrastructure

- Docker / Docker Compose
- Maven

---

## Service Discovery

The current version uses **Netflix Eureka** for service registration and discovery.

`matching-service` communicates with other services through Feign clients using logical service names:

```java
@FeignClient(name = "job-service")
```

The API Gateway follows the same pattern, using load-balanced routes such as:

```text
lb://JOB-SERVICE
```

Instead of hardcoding an instance address, Spring Cloud LoadBalancer resolves an available `job-service` instance through Eureka's service registry, so services address each other by logical name rather than fixed IPs/ports.

---

## API Gateway

The gateway (`gatewayserver`) defines routes explicitly in code (`GatewayServerApplication`), rather than relying on Eureka's discovery locator — this avoids automatically exposing every registered service to the outside world.

| Route prefix             | Target                  | Resilience strategy                                          |
| ------------------------ | ----------------------- | ------------------------------------------------------------ |
| `/jobsearch/users/**`    | `lb://USER-SERVICE`     | Circuit breaker → fallback (`/contactSupport`)               |
| `/jobsearch/jobs/**`     | `lb://JOB-SERVICE`      | Retry (3x, GET only, exponential backoff) + Redis token-bucket rate limiter (20 req/s, burst 40, keyed by `X-User-Id` or client IP) |
| `/jobsearch/matching/**` | `lb://MATCHING-SERVICE` | Circuit breaker → fallback (`/contactSupport`), wider timeout (30s) to accommodate the AI scoring call chain |

Each route strategy is chosen deliberately for the shape of its traffic: resume uploads are slow but non-idempotent (circuit breaker only), job search is read-heavy and idempotent (safe to retry, needs rate limiting to protect the DB), and matching has the longest call chain (user + job + AI), so it gets the most headroom before tripping.

---


## Matching & AI Integration

`matching-service` fans out over Feign to `user-service` and `job-service`, then scores candidates and persists the result:

```text
Matching Service
       |
       |-- Feign --> User Service   (candidate profile)
       |-- Feign --> Job Service    (candidate jobs, /api/search)
       |-- Feign --> AI Service     (scoring)
       |
       v
   Persist match result (matchingdb)
       |
       v
   Publish "match-computed" event to Kafka
```

`ai-service` is not implemented yet, but its contract is already defined and wired in:

```java
@FeignClient(name = "ai-service", fallback = AiFallback.class)
public interface AiFeignClient {
    @PostMapping("/api/ai/score")
    ScoreResponse score(@RequestBody ScoreRequest request);
}
```

Until `ai-service` exists, calls fall through to a rule-based scorer, so the rest of the pipeline — orchestration, caching, persistence, event publishing — runs and is testable end-to-end without depending on the AI component. Matching parameters (`candidateLimit`, `minScore`, `topN`, cache TTL) are externalized in Config Server so they can be tuned without a redeploy.

---

## Event-Driven Notifications

`matching-service` publishes `match-computed` to Kafka. `notification-service` binds two independent Spring Cloud Stream consumers to that topic, each under its own consumer group:

```java
@Bean
public Consumer<MatchComputedEvent> emailNotification(NotificationSender sender) { ... }

@Bean
public Consumer<MatchComputedEvent> smsNotification(NotificationSender sender) { ... }
```

Email and SMS are separate consumers rather than a composed function chain, so a failure in one channel doesn't block the other — each retries, dead-letters, and scales independently.

---

## Configuration

`configserver` runs on the `native` profile, serving config from `classpath:/config` (one YAML per service). A `git` backend is present but commented out, ready to switch on when config needs to live outside the jar.

Encrypted values (`{cipher}...`) are decrypted using a key read from `CONFIG_ENCRYPT_KEY`. There is no default — the server fails to start rather than falling back to a weak key.

---

## Project Structure

```text
job-search-platform/
│
├── configserver/
├── eurekaserver/
├── gatewayserver/
├── job-service/
├── matching-service/
├── notification-service/
├── user-service/
│
├── infra/
│   └── postgres-init.sql      # creates userdb / jobdb / matchingdb
├── docker-compose.yml
├── verify.sh                  # build + test all modules (or a single one)
└── README.md
```

Each service is an independent Spring Boot application with its own Maven build.

---

## Local Infrastructure

```bash
docker compose up -d      # start
docker compose ps         # check status
docker compose down       # stop
```

Provides:

- **PostgreSQL 17** — `userdb`, `jobdb`, `matchingdb`, created via `infra/postgres-init.sql`
- **Redis 7**
- **Apache Kafka** (KRaft single-node — broker and controller in one process)
- **Kafka UI** — `http://localhost:8081`
- *(commented out)* **Keycloak** — scaffolded for OAuth2/JWT on the gateway, not yet wired in

---

## Configuration & Secrets

Create a `.env` file in the project root (excluded from Git):

```env
DB_PASSWORD=your_local_database_password
```

| Variable             | Used by               | Default                              |
| -------------------- | --------------------- | ------------------------------------ |
| `DB_PASSWORD`        | all business services | —                                    |
| `REDIS_HOST`         | `gatewayserver`       | `localhost`                          |
| `EUREKA_HOST`        | `gatewayserver`       | `localhost`                          |
| `CONFIG_ENCRYPT_KEY` | `configserver`        | none — required, fails fast if unset |

Do not commit real credentials, API keys, or tokens.

---

## Running Locally

```text
1. docker compose up -d
2. EurekaServerApplication
3. ConfigServerApplication
4. UserServiceApplication / JobServiceApplication / MatchingServiceApplication / NotificationServiceApplication
5. GatewayServerApplication
```

Services register with Eureka on startup and resolve each other by logical name. Swagger UI is available on `user-service`, `job-service`, and `matching-service`.

### Build & test

```bash
./verify.sh              # compile + test all 7 modules
./verify.sh compile      # compile only
./verify.sh job-service  # single module
```

---

## Roadmap

Current:

```text
Spring Boot Microservices → Spring Cloud Gateway → Eureka Discovery → Feign + LoadBalancer → PostgreSQL / Redis / Kafka
```

Planned:

```text
Eureka Discovery → Kubernetes Deployment → Spring Cloud Kubernetes Discovery → Kubernetes-native Service Management
```

- Implement `ai-service` behind the existing `AiFeignClient` contract
- OAuth2/JWT auth via Keycloak at the gateway
- Kubernetes Deployments and Services, Kubernetes-based discovery
- Health checks and self-healing
- Centralized observability (metrics/tracing/logging aggregation)

---

## Status

🚧 Active development. Current state: Eureka-based discovery, explicit-routing gateway with per-route resilience policies, event-driven notifications, and a contract-first integration point for AI matching. Kubernetes migration is the next infrastructure milestone.