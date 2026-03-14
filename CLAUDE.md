# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Build & Development Commands

```bash
# Build all services (skip tests)
./mvnw clean package -DskipTests

# Run all tests (unit + integration)
./mvnw verify

# Run unit tests only
./mvnw test

# Run integration tests only
./mvnw verify -P integration

# Build a single service
./mvnw clean package -DskipTests -pl services/customer-service

# Run tests for a single service
./mvnw test -pl services/customer-service

# Run a single test class
./mvnw test -pl services/customer-service -Dtest=CustomerServiceTest

# Code quality check
./mvnw checkstyle:check
```

## Local Dev Environment

```bash
make docker-up      # Start all infra + creates Kafka topics (waits 30s for healthy)
make docker-down    # Stop everything
make kafka-topics   # Re-create Kafka topics against running cluster (exec into kafka-1)
make docker-rebuild # Full teardown → rebuild images → restart
```

No `mvnw` wrapper exists yet — use `mvn` directly or generate it with `mvn wrapper:wrapper`.

---

## Architecture Overview

**Maven multi-module monorepo.** Root `pom.xml` (groupId `com.eip`, artifactId `insurance-platform:1.0.0`) is the parent for all 14 services under `services/`. Spring Boot 3.2.3, Java 21, Spring Cloud 2023.0.0.

### Service Map

| Service | Port | DB Port | Role |
|---|---|---|---|
| customer-service | 8081 | 5432 | Customer onboarding, KYC, OFAC screening |
| policy-service | 8082 | 5433 | Policy lifecycle; **Event Sourcing + CQRS** |
| premium-calc-service | 8083 | 5445 | Rating engine; Caffeine cache (1hr TTL) |
| claims-service | 8084 | 5434 | Claims adjudication; 4-eyes approval >$10K |
| payment-service | 8085 | 5435 | Stripe integration; idempotency key UNIQUE constraint |
| billing-service | 8086 | 5436 | Invoice generation; commission calc |
| document-service | 8087 | 5438 | PDF generation (iText); GCS storage |
| fraud-detection-service | 8088 | 5437 | Redis velocity counters (INCR + TTL) |
| notification-service | 8089 | 5440 | SendGrid (email) + Twilio (SMS) |
| reference-data-service | 8090 | 5441 | Rate tables, state rules; Caffeine cache (4hr TTL) |
| broker-service | 8091 | 5442 | Broker/agent management, commissions |
| audit-service | 8092 | 5443 | Append-only audit log (consumes ALL domain topics) |
| analytics-service | 8093 | 5444 | OLAP read model; denormalized metrics tables |
| workflow-orchestrator | 8094 | 5446 | Saga orchestration (PolicyIssuance + ClaimProcessing) |

### Cross-Cutting Patterns Applied Uniformly

**Every service** follows the same structure:
- `config/SecurityConfig.java` — stateless JWT (Keycloak RS256, `spring-boot-starter-oauth2-resource-server`), CSRF disabled, actuator endpoints public
- `config/KafkaConfig.java` — explicit `ConsumerFactory` + `ConcurrentKafkaListenerContainerFactory` (AckMode.RECORD, concurrency=3)
- `exception/GlobalExceptionHandler.java` — `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`
- `db/migration/V1__initial_schema.sql` — Flyway; `ddl-auto: validate` in JPA (schema only managed by Flyway)
- All DTOs are **Java 21 records**
- All mutable JPA entities have `@Version Long version` for optimistic locking
- Actuator exposes: `health, info, prometheus, metrics`

### Transactional Outbox Pattern

Used in: **customer, policy, payment, billing, claims** services.

`OutboxEvent` entity saved in same transaction as business entity. `OutboxPublisher` (`@Scheduled(fixedDelay = 100)`) polls for `status=PENDING` events and publishes to Kafka, then marks `PROCESSED`. This prevents event loss on service crash.

Services without outbox (consume-only or command-side only): **audit, analytics, notification, document, fraud-detection, reference-data, workflow-orchestrator**.

### Event Sourcing + CQRS (policy-service only)

- `policy_events` table: append-only event store
- `policy_projections` table: denormalized read model rebuilt by replaying events
- No other service uses event sourcing — don't add it elsewhere without explicit requirement

### Saga Orchestration (workflow-orchestrator)

Two sagas managed by `SagaOrchestrationService`:
1. **PolicyIssuanceSaga** (6 steps): QUOTE_CREATED → PREMIUM_CALCULATED → PAYMENT_COLLECTED → POLICY_ISSUED → BILLING_CREATED → DOCUMENTS_GENERATED
2. **ClaimProcessingSaga** (8 steps): FNOL_RECEIVED → FRAUD_SCORED → COVERAGE_VERIFIED → ADJUSTER_ASSIGNED → RESERVE_SET → APPROVED → PAYMENT_INITIATED → CLOSED

State stored in `saga_instances` + `saga_steps` tables. On failure: compensating commands emitted in reverse step order. Saga start is idempotent (unique index on `correlation_id + saga_type`).

### Kafka Topic Naming Convention

`{domain}.{entity-plural}.{event-verb}` — e.g. `policy.policies.issued`, `payment.payments.failed`, `fraud.scores.returned`.

Partitions: high-volume topics (policy, payment, claims) get 24 partitions; audit/reference get 3-6. All topics replication-factor 3.

**Consumer group IDs**: `cg-customer`, `cg-policy-payment`, `cg-billing-policy`, `cg-billing-payment`, `cg-fraud-detection`, `cg-audit`, `cg-analytics`, `cg-workflow`, `cg-claims-fraud`, `cg-claims-payment`, `cg-notification`, `cg-document`.

### Security

- **Auth**: Keycloak at port 8080, realm `insurance`, RS256 JWTs
- **JWKS URI env var**: `KEYCLOAK_JWK_URI` (default: `http://localhost:8080/realms/insurance/protocol/openid-connect/certs`)
- **PII**: SSN BCrypt-hashed in customer-service before persistence
- **Roles enforced via**: `@PreAuthorize` at controller method level (method security enabled in `SecurityConfig`)

### Infrastructure (under `infrastructure/`)

| Path | Contents |
|---|---|
| `infrastructure/kafka/schemas/` | Avro schemas: customer-registered, policy-issued, claim-filed, payment-completed |
| `infrastructure/monitoring/` | `prometheus.yml` scraping all 14 `/actuator/prometheus` endpoints; Grafana provisioning |
| `infrastructure/terraform/` | GKE Autopilot + 14 Cloud SQL instances (GCP); state in GCS |
| `infrastructure/kubernetes/` | Kustomize base + overlays (dev/staging/prod); HPA + PDB per service; NetworkPolicy |
| `infrastructure/keycloak/` | Realm config |

### DB Credentials

All services use env vars `DB_USERNAME` / `DB_PASSWORD` (default `eip_user` / `eip_pass`). DB name pattern: `{service-name}_db` (e.g. `analytics_db`, `workflow_db`). Kafka bootstrap: `KAFKA_BOOTSTRAP_SERVERS` (default `localhost:29092`).

---

## Key Implementation Details

- **premium-calc-service**: has `V2__seed_rate_tables.sql` in addition to V1
- **reference-data-service**: has `V2__seed_reference_data.sql` in addition to V1
- **payment-service**: `UNIQUE` constraint on `idempotency_key` column — idempotency enforced at DB level
- **fraud-detection-service**: Redis (`INCR` + `EXPIRE`) for velocity counters; port 6379
- **claims-service**: `approveClaim()` requires second approver when amount > $10,000 (4-eyes rule)
- **analytics-service**: pure Kafka consumer — no outbox, no command API, read-only REST endpoints
