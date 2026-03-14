# Insurance Platform — Generation Progress

Last updated: 2026-03-14

---

## Services (14/14 Complete)

| Service | Port | Package | DB Port | Status |
|---|---|---|---|---|
| customer-service | 8081 | com.eip.customer | 5432 | ✅ Complete |
| policy-service | 8082 | com.eip.policy | 5433 | ✅ Complete |
| premium-calc-service | 8083 | com.eip.premiumcalc | 5445 | ✅ Complete |
| claims-service | 8084 | com.eip.claims | 5434 | ✅ Complete |
| payment-service | 8085 | com.eip.payment | 5435 | ✅ Complete |
| billing-service | 8086 | com.eip.billing | 5436 | ✅ Complete |
| document-service | 8087 | com.eip.document | 5438 | ✅ Complete |
| fraud-detection-service | 8088 | com.eip.fraud | 5437 | ✅ Complete |
| notification-service | 8089 | com.eip.notification | 5440 | ✅ Complete |
| reference-data-service | 8090 | com.eip.referencedata | 5441 | ✅ Complete |
| broker-service | 8091 | com.eip.broker | 5442 | ✅ Complete |
| audit-service | 8092 | com.eip.audit | 5443 | ✅ Complete |
| analytics-service | 8093 | com.eip.analytics | 5444 | ✅ Complete |
| workflow-orchestrator | 8094 | com.eip.workflow | 5446 | ✅ Complete |

---

## Infrastructure (5/5 Complete)

| Component | Path | Status |
|---|---|---|
| Docker Compose | `docker-compose.yml` | ✅ Complete |
| Kubernetes manifests | `infrastructure/kubernetes/` | ✅ Complete |
| Terraform configs | `infrastructure/terraform/` | ✅ Complete |
| Kafka Avro schemas | `infrastructure/kafka/schemas/` | ✅ Complete |
| Monitoring configs | `infrastructure/monitoring/` | ✅ Complete |

---

## Remaining Work Detail

### analytics-service (port 8093, DB port 5444)
- Package: `com.eip.analytics`
- Entities: `PolicyMetrics`, `ClaimMetrics`, `BrokerMetrics` (OLAP read model, denormalized)
- Kafka consumers: consume events from all domain topics and aggregate into metrics tables
- Controller: GET /api/v1/analytics/policies, /claims, /brokers — summary and time-series endpoints
- No outbox pattern (read-only aggregation side)
- `V1__initial_schema.sql`: policy_metrics, claim_metrics, broker_metrics tables

### workflow-orchestrator (port 8094, DB port 5446)
- Package: `com.eip.workflow`
- Entities: `SagaInstance`, `SagaStep`
- Sagas:
  - `PolicyIssuanceSaga` — 6 steps: QUOTE_CREATED → PREMIUM_CALCULATED → PAYMENT_COLLECTED → POLICY_ISSUED → BILLING_CREATED → DOCUMENTS_GENERATED
  - `ClaimProcessingSaga` — 8 steps: FNOL_RECEIVED → FRAUD_SCORED → COVERAGE_VERIFIED → ADJUSTER_ASSIGNED → RESERVE_SET → APPROVED → PAYMENT_INITIATED → CLOSED
- `SagaOrchestrationService` — state machine transitions, compensating transactions on failure
- Kafka consumer listening to all saga-relevant events to advance saga state
- `V1__initial_schema.sql`: saga_instances, saga_steps tables

### docker-compose.yml (root of project)
- 14 PostgreSQL instances (ports 5432–5446, one per service)
- 3-node Kafka KRaft cluster (ports 29092, 29093, 29094)
- Schema Registry (port 8085 mapped internally)
- Kafka UI (port 9090)
- Redis (port 6379)
- Keycloak (port 8080) with insurance realm
- Prometheus (port 9091)
- Grafana (port 3000)
- Jaeger (port 16686)
- All 14 Spring Boot services with health checks and depends_on

### infrastructure/kubernetes/
- `namespaces.yaml` — ns-gateway, ns-identity, ns-tier0, ns-tier1, ns-tier2, ns-analytics, ns-infra, ns-observability
- Deployments with `HorizontalPodAutoscaler` and `PodDisruptionBudget` for each service
- `kustomization.yaml` base + overlays (dev, staging, prod)
- ConfigMaps for Kafka bootstrap, Keycloak JWKS URI
- NetworkPolicy to restrict cross-namespace traffic

### infrastructure/terraform/
- `main.tf` — provider, backend (GCS state), project config
- `variables.tf` — project_id, region, environment, cluster_name
- `gke.tf` — GKE Autopilot cluster with node pools
- `cloud-sql.tf` — 14 Cloud SQL PostgreSQL instances (or shared with databases)

### infrastructure/kafka/schemas/
- `customer-registered.avsc`
- `policy-issued.avsc`
- `claim-filed.avsc`
- `payment-completed.avsc`

### infrastructure/monitoring/
- `prometheus.yml` — scrape configs for all 14 services on their /actuator/prometheus endpoints
- `grafana/provisioning/datasources/prometheus.yml`
- `grafana/provisioning/dashboards/insurance-platform.json` — key panels: request rate, error rate, DB pool, Kafka lag

---

## Key Design Decisions (for reference)

- **Event Sourcing + CQRS**: policy-service uses append-only `policy_events` + `policy_projections` read model
- **Outbox Pattern**: customer, policy, payment, billing, claims services all use transactional outbox (100ms polling)
- **Kafka Consumer Groups**: cg-customer, cg-policy-payment, cg-billing-policy, cg-billing-payment, cg-fraud-detection, cg-audit, cg-claims-fraud, cg-claims-payment, cg-notification, cg-document
- **JWT Auth**: Keycloak RS256, Spring Security OAuth2 Resource Server in every service
- **4-eyes approval**: Claims > $10K require second approver in ClaimsService.approveClaim()
- **Idempotency**: payment-service has UNIQUE constraint on `idempotency_key`
- **Redis**: fraud-detection-service uses Redis INCR with TTL for velocity counters
- **Caffeine cache**: premium-calc-service (rate tables, 1hr TTL), reference-data-service (products/state-rules, 4hr TTL)
- **BCrypt**: SSN hashed in customer-service before persistence
- **Flyway**: V1__initial_schema.sql in every service; premium-calc has V2__seed_rate_tables.sql; reference-data has V2__seed_reference_data.sql
- **ProblemDetail (RFC 7807)**: GlobalExceptionHandler in every service
- **@Version**: optimistic locking on all mutable JPA entities
- **Java 21 Records**: all DTOs are records
- **Spring Boot 3.2.3 / Spring Cloud 2023.0.0 / Maven multi-module**
- **Group ID**: `com.eip`, parent artifact `insurance-platform:1.0.0`

---

## Resume Instructions

Next session: start with `analytics-service`, then `workflow-orchestrator`, then `docker-compose.yml`, then infrastructure folders.
Recommended order:
1. `analytics-service` — simple Kafka consumer + aggregation, no outbox
2. `workflow-orchestrator` — most complex, saga state machine
3. `docker-compose.yml` — largest single file
4. `infrastructure/kafka/schemas/` — 4 Avro files, quick
5. `infrastructure/monitoring/` — prometheus.yml + Grafana
6. `infrastructure/terraform/` — 4 .tf files
7. `infrastructure/kubernetes/` — most YAML, do last
