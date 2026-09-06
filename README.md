# Job Search Platform

A Spring Boot microservices backend for job discovery, candidate profiles, resume processing, job matching, and notifications. The current architecture replaces Eureka-based service discovery with **Spring Cloud Kubernetes DiscoveryClient + DiscoveryServer**, while retaining Spring Cloud Gateway, OpenFeign, Config Server, PostgreSQL, Redis, Kafka, Resilience4j, Flyway, and Actuator.

> **Current stage:** core Java services and Kubernetes deployment manifests are implemented. `ai-service` is a planned downstream service; when it is unavailable, `matching-service` can fall back to rule-based scoring.

## Architecture

```text
External Client
      |
      v
Spring Cloud Gateway (8072)
      |
      |  lb://service-name
      v
Spring Cloud LoadBalancer
      |
      v
Spring Cloud Kubernetes DiscoveryClient
      |
      v
Kubernetes DiscoveryServer
      |
      v
Kubernetes API
      |
      +--> Service / Endpoint --> Pod

Business flow
-------------
Gateway
  +--> user-service (8080) ------> PostgreSQL
  |          |                    Kafka: resume-uploaded / resume-parsed
  |          +------------------> resume storage
  |
  +--> job-service (8090) -------> PostgreSQL + Redis
  |          +------------------> Kafka: job-posted
  |
  +--> matching-service (9000) --> user-service (Feign)
             |                    job-service (Feign)
             |                    ai-service (Feign, planned)
             +------------------> PostgreSQL + Redis
             +------------------> Kafka: match-computed
                                      |
                                      v
                              notification-service (9010)
```

### Kubernetes service discovery

`gatewayserver` and `matching-service` use the HTTP-based Spring Cloud Kubernetes `DiscoveryClient`. Instead of registering application instances with Eureka, Kubernetes owns service membership. The DiscoveryServer reads Kubernetes Service, Endpoint, and Pod metadata through the Kubernetes API and exposes that information to DiscoveryClient consumers.

Example runtime path:

```text
matching-service
  -> @FeignClient(name = "job-service")
  -> Spring Cloud LoadBalancer
  -> Kubernetes DiscoveryClient
  -> DiscoveryServer
  -> Kubernetes API
  -> job-service Service / Endpoint
  -> job-service Pod
```

The DiscoveryServer runs with a namespace-scoped ServiceAccount, Role, and RoleBinding defined in `k8s/01-discoveryserver.yaml`.

## Services

| Service | Port | Responsibility |
|---|---:|---|
| `configserver` | 8071 | Centralized configuration using the native backend for local development |
| `gatewayserver` | 8072 | API routing, Redis rate limiting, circuit breaking, retry, and request correlation |
| `user-service` | 8080 | User profiles, resume upload, resume state, and resume-processing events |
| `job-service` | 8090 | Job CRUD/search, PostgreSQL persistence, Redis caching, and job events |
| `matching-service` | 9000 | Feign aggregation, matching persistence/cache, AI integration boundary, and rule-based fallback |
| `notification-service` | 9010 | Kafka consumer for match notifications with email/SMS channels and DLQ configuration |
| `ai-service` | 9100 | Planned resume parsing and AI scoring service |
| Kubernetes DiscoveryServer | 8761 container port | HTTP service-discovery bridge between Spring Cloud clients and the Kubernetes API |

## Main API routes

Requests enter through `gatewayserver` and are rewritten before reaching each service.

| Gateway route | Downstream operation |
|---|---|
| `POST /jobsearch/users/api/create` | Create a user |
| `GET /jobsearch/users/api/{userId}` | Get a user profile |
| `POST /jobsearch/users/api/{userId}/resume` | Upload a resume for asynchronous processing |
| `GET /jobsearch/users/api/{userId}/resume` | Get the latest resume state |
| `POST /jobsearch/jobs/api/create` | Create a job |
| `GET /jobsearch/jobs/api/{jobId}` | Get a job |
| `GET /jobsearch/jobs/api/search` | Search jobs using optional filters |
| `PUT /jobsearch/jobs/api/{jobId}` | Update a job |
| `DELETE /jobsearch/jobs/api/{jobId}` | Soft-close a job |
| `POST /jobsearch/matching/api/compute?userId={id}` | Compute matches |
| `GET /jobsearch/matching/api/matches?userId={id}` | Read stored matches |

## Event flows

```text
resume-uploaded : user-service      -> ai-service (planned)
resume-parsed   : ai-service        -> user-service
match-computed  : matching-service  -> notification-service
job-posted      : job-service       -> future asynchronous consumers
```

`notification-service` configures separate email and SMS consumer groups for `match-computed`. Consumer failures can be routed to dedicated Kafka dead-letter topics. `user-service` also configures a DLQ for failed `resume-parsed` consumption.

## Resilience and data

- **Gateway:** Redis-backed request rate limiting, retry for job GET traffic, and circuit-breaker fallbacks.
- **Job service:** Resilience4j protection around search operations and Redis caching for read-heavy job data.
- **Matching service:** OpenFeign clients for service-to-service calls, circuit-breaker support, Redis caching, and rule-based fallback when the planned AI service is unavailable.
- **Persistence:** PostgreSQL databases for users, jobs, and matching results; Flyway manages schema migrations.
- **Observability:** Spring Boot Actuator, Prometheus registry, correlation IDs at the gateway, and OpenTelemetry Java agent dependencies.

## Repository structure

```text
job-search-platform/
├── configserver/
├── gatewayserver/
├── user-service/
├── job-service/
├── matching-service/
├── notification-service/
├── infra/
│   └── postgres-init.sql
├── k8s/
│   ├── 00-namespace.yaml
│   ├── 01-discoveryserver.yaml
│   ├── 01-secret.example.yaml
│   ├── 02-postgres.yaml
│   ├── 03-redis.yaml
│   ├── 04-kafka.yaml
│   ├── 05-configserver.yaml
│   ├── 06-user-service.yaml
│   ├── 07-job-service.yaml
│   ├── 08-matching-service.yaml
│   ├── 09-notification-service.yaml
│   ├── 10-gatewayserver.yaml
│   ├── 11-gateway-nodeport.yaml
│   └── kustomization.yaml
├── docker-compose.yml
└── verify.sh
```

## Local development

Docker Compose provides PostgreSQL, Redis, Kafka, and Kafka UI. The Spring Boot services can then run from the IDE or with their Maven wrappers.

Create a local `.env` file (it is ignored by Git):

```env
DB_PASSWORD=your-local-password
```

Start infrastructure:

```bash
docker compose up -d
```

Then start `configserver` followed by the business services and `gatewayserver`.

> The Kubernetes DiscoveryServer is designed for the Kubernetes deployment path. For full service-discovery behavior, deploy the stack to a Kubernetes cluster rather than treating DiscoveryServer as a replacement for Kubernetes itself.

Useful local endpoints:

```text
Gateway:             http://localhost:8072
Config Server:       http://localhost:8071
User Service health: http://localhost:8080/actuator/health
Job Swagger UI:      http://localhost:8090/swagger-ui.html
Kafka UI:            http://localhost:8081
```

## Kubernetes deployment

The manifests use namespace `jobsearch`. Application images are referenced as `jobsearch/<service>:v1` and can be built with each module's Jib Maven plugin before deployment to a cluster that can access those images.

### 1. Create the namespace and runtime Secret

Do not commit real credentials. Create the Secret directly in the cluster:

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl -n jobsearch create secret generic jobsearch-secrets \
  --from-literal=DB_USER=jobsearch \
  --from-literal=DB_PASSWORD='<your-db-password>' \
  --from-literal=CONFIG_ENCRYPT_KEY='<your-config-encryption-key>'
```

`k8s/01-secret.example.yaml` documents the expected keys only and is intentionally excluded from `kustomization.yaml`.

### 2. Apply the platform

```bash
kubectl apply -k k8s
```

### 3. Verify discovery

```bash
kubectl -n jobsearch get pods
kubectl -n jobsearch get services
kubectl -n jobsearch port-forward svc/spring-cloud-kubernetes-discoveryserver 8761:80
```

In another terminal:

```bash
curl http://localhost:8761/apps
```

The response should contain Kubernetes services visible to the DiscoveryServer in the `jobsearch` namespace.

### 4. Access the gateway

`k8s/11-gateway-nodeport.yaml` exposes the gateway for a local/demo cluster. The exact host address depends on the Kubernetes environment.

## Example requests

```bash
curl -X POST http://localhost:8072/jobsearch/jobs/api/create \
  -H 'Content-Type: application/json' \
  -d '{"title":"Backend Software Engineer","company":"Acme","city":"Seattle","minSalary":120000,"maxSalary":160000,"requiredYears":2,"description":"Build backend services","skills":["Java","Spring Boot","Kafka"]}'

curl 'http://localhost:8072/jobsearch/jobs/api/search?city=Seattle&skill=Kafka'

curl -X POST http://localhost:8072/jobsearch/users/api/create \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo User","email":"demo@example.com","phone":"2065550100","city":"Seattle"}'

curl -X POST 'http://localhost:8072/jobsearch/matching/api/compute?userId=1'
```

## Security notes

- Database passwords are supplied through environment variables or Kubernetes Secrets; no runtime password is committed in application configuration.
- `SecurityConfig` currently permits requests as a development placeholder. OAuth2/Keycloak dependencies are present, but production authentication is not yet enabled.
- The sample Kubernetes Secret contains placeholders only. Real secrets should be created outside Git.
- The local Config Server uses the `native` backend. A production deployment should use an externalized and appropriately secured configuration source.

## Current limitations / roadmap

- Implement `ai-service` for resume parsing and model-based matching.
- Enable OAuth2/JWT authentication and authorization.
- Add broader unit, integration, and contract test coverage.
- Add CI/CD and image publishing for Kubernetes environments.
- Replace demo/local storage choices such as `emptyDir` resume/PostgreSQL volumes with production-grade persistent storage.
- Extend observability dashboards, tracing export, and operational alerts.

## Technology stack

Java 21 · Spring Boot 4 · Spring Cloud 2025.1.x · Spring Cloud Gateway · Spring Cloud Kubernetes · OpenFeign · Resilience4j · PostgreSQL · Flyway · Redis · Apache Kafka · Spring Cloud Stream · Docker Compose · Kubernetes · Kustomize · Maven · Jib · Actuator · Prometheus · OpenTelemetry
