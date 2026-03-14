# Enterprise Insurance Platform — Master Architecture Document

**Version**: 1.0 | **Date**: 2026-03-14 | **Author**: Principal Software Architect

---

## PART 1 — PRODUCT REQUIREMENTS DOCUMENT (PRD)

---

### 1. Product Vision

Build a **cloud-native, microservices-based enterprise insurance platform** that handles the full insurance lifecycle — from customer onboarding through policy issuance, claims adjudication, and payments — at web scale. The platform serves as both a production-grade insurance system and a **canonical reference implementation** of distributed systems engineering, teaching microservices patterns, event-driven architecture, and enterprise Java best practices through real working code.

**Vision Statement**: *"Deliver insurance services at the speed of cloud: every policy quoted in milliseconds, every claim processed transparently, every customer served reliably at global scale."*

---

### 2. Business Objectives

| # | Objective | Measurable Target |
|---|-----------|-------------------|
| B1 | Reduce policy issuance time | Quote-to-bind < 30 seconds end-to-end |
| B2 | Automate claims straight-through processing | 60% of low-complexity claims auto-adjudicated |
| B3 | Reduce fraud losses | Fraud detection blocks 85%+ of synthetic fraud attempts |
| B4 | Platform revenue growth | Support 1M active policies, 100K+ transactions/day |
| B5 | Broker productivity | Brokers manage 3x more policies vs manual processes |
| B6 | Regulatory compliance | 100% audit trail coverage, zero compliance violations |
| B7 | Developer enablement | New service from scaffold to production in < 1 week |

---

### 3. Stakeholders

| Stakeholder | Role | Key Concerns |
|-------------|------|--------------|
| CTO | Technology sponsor | Architecture quality, scalability, cost |
| VP of Insurance Operations | Business sponsor | Claims SLA, underwriting accuracy, compliance |
| Chief Actuary | Domain expert | Premium calculation accuracy, reserving |
| Chief Compliance Officer | Regulatory | GDPR, SOX, NAIC, state regulations |
| CISO | Security | Data protection, breach prevention, audit |
| Engineering Teams | Builders | Developer experience, clear boundaries |
| Brokers / Agents | External | Fast quoting, commission transparency |
| Policyholders | End users | Self-service, fast claims, clear billing |
| Reinsurers | Partners | Bordereaux data, exposure reporting |
| Regulators (State DOIs) | Overseers | Filing compliance, rate justification |

---

### 4. User Personas

**P1 — Alex (Policyholder)**
- Wants: Quick quote, clear invoice, fast claim settlement, mobile-friendly portal
- Pain points: Opaque claim status, paper documents, long hold times

**P2 — Sarah (Insurance Broker)**
- Wants: Instant quoting for multiple carriers, commission visibility, bulk policy management
- Pain points: Re-keying data across systems, delayed commission statements

**P3 — Mike (Claims Adjuster)**
- Wants: Full claim history, fraud score, automated reserve calculation, document access
- Pain points: Swivel-chair between systems, manual fraud checks

**P4 — Diana (Underwriter)**
- Wants: Actuarial data, risk scoring, real-time portfolio view, exception workflow
- Pain points: Slow rate-table updates, limited analytics

**P5 — Omar (Insurance Admin / Ops)**
- Wants: System health visibility, policy/claim overrides, user management, audit logs
- Pain points: No single pane of glass, manual reporting

**P6 — Rachel (Compliance Officer)**
- Wants: Complete audit trails, regulatory report generation, data residency controls
- Pain points: Manual evidence gathering for audits

---

### 5. Functional Requirements

#### 5.1 Customer Onboarding Service
- FR-CO-01: Customer self-registration (name, DOB, contact, SSN-hash)
- FR-CO-02: KYC verification via third-party (Jumio/Onfido) — identity + liveness check
- FR-CO-03: OFAC sanctions screening on registration
- FR-CO-04: Duplicate detection (fuzzy name + DOB + address matching)
- FR-CO-05: Credit bureau soft pull for underwriting eligibility
- FR-CO-06: Customer profile CRUD with full audit history
- FR-CO-07: GDPR data export and right-to-erasure workflow
- FR-CO-08: Multi-channel onboarding: portal, broker-assisted, API

#### 5.2 Policy Management Service
- FR-PM-01: Multi-line product support (Auto, Home, Life, Health, Commercial)
- FR-PM-02: Quote generation with versioned rate tables
- FR-PM-03: Quote-to-bind workflow with e-signature (DocuSign)
- FR-PM-04: Policy issuance — policy number generation, document generation
- FR-PM-05: Endorsement (mid-term changes) with pro-rata premium adjustment
- FR-PM-06: Cancellation, lapse, reinstatement workflows
- FR-PM-07: Renewal processing (automated + broker-assisted)
- FR-PM-08: Multi-driver, multi-vehicle support (Auto)
- FR-PM-09: Policy search by customer, broker, date range, status
- FR-PM-10: Event Sourcing — full temporal history of every policy state change

#### 5.3 Premium Calculation Service
- FR-PC-01: Rule-based rating engine (ISO/AAIS rating manuals)
- FR-PC-02: Rate table management with effective dating and versioning
- FR-PC-03: Multi-factor rating: territory, credit score, claim history, vehicle class
- FR-PC-04: Actuarial override capability with approval workflow
- FR-PC-05: Batch re-rating for renewal portfolio
- FR-PC-06: Sub-second quote response for real-time quoting
- FR-PC-07: Discount and surcharge rule engine

#### 5.4 Claims Management Service
- FR-CL-01: First Notice of Loss (FNOL) — web, mobile, phone-to-digital
- FR-CL-02: Claim number assignment (unique, formatted per line of business)
- FR-CL-03: Coverage verification against active policy at date of loss
- FR-CL-04: Claim assignment to adjuster (rule-based: complexity, geography, capacity)
- FR-CL-05: Reserve establishment and adjustment workflow
- FR-CL-06: Subrogation / salvage tracking
- FR-CL-07: Litigation flag and legal hold
- FR-CL-08: ISO ClaimSearch integration — prior claim history lookup
- FR-CL-09: Claim payment authorization workflow (4-eyes for amounts > $10K)
- FR-CL-10: Claim close and reopen workflow

#### 5.5 Payment Processing Service
- FR-PAY-01: Premium collection (credit card, ACH, wire) via Stripe / Braintree
- FR-PAY-02: Claim disbursement (check, ACH, Zelle)
- FR-PAY-03: Idempotent payment submission (prevent double-payment)
- FR-PAY-04: Payment retry with exponential backoff
- FR-PAY-05: Refund processing for cancelled policies
- FR-PAY-06: Payment reconciliation with bank statement import
- FR-PAY-07: PCI-DSS compliance — card data never stored, tokenized via payment processor

#### 5.6 Billing Service
- FR-BL-01: Billing account creation linked to policy
- FR-BL-02: Invoice generation (monthly, quarterly, annual)
- FR-BL-03: Installment plan calculation
- FR-BL-04: Late payment grace period, lapse notice generation
- FR-BL-05: EFT authorization management
- FR-BL-06: Commission calculation and broker remittance

#### 5.7 Document Management Service
- FR-DM-01: Policy document generation (PDF via Jasper/iText)
- FR-DM-02: Document versioning with content-addressable storage
- FR-DM-03: GCS (Google Cloud Storage) for object storage
- FR-DM-04: Virus scanning on all uploaded documents
- FR-DM-05: E-signature integration (DocuSign)
- FR-DM-06: Document classification via ML tagging
- FR-DM-07: Document retention rules per document type (7 years financial, 10 years claims)

#### 5.8 Notification Service
- FR-NT-01: Email (SendGrid) — policy docs, invoices, claim updates
- FR-NT-02: SMS (Twilio) — payment reminders, FNOL confirmation
- FR-NT-03: Push notifications (FCM/APNS) — mobile app
- FR-NT-04: In-app real-time alerts via WebSocket / SSE
- FR-NT-05: Notification templates with i18n support
- FR-NT-06: Notification preference management per customer
- FR-NT-07: Delivery tracking and retry on failure

#### 5.9 Fraud Detection Service
- FR-FD-01: Real-time fraud scoring on FNOL (< 200ms SLA)
- FR-FD-02: Rule-based scoring engine (Phase 1)
- FR-FD-03: ML model scoring (TensorFlow Serving, Phase 3)
- FR-FD-04: Velocity checks: claims per customer, claims per address, claims per broker
- FR-FD-05: Network analysis: shared addresses, phones, banking accounts across claims
- FR-FD-06: OFAC/sanctions check on claimants
- FR-FD-07: Fraud case management workflow
- FR-FD-08: SIU (Special Investigations Unit) referral workflow

#### 5.10 Reporting and Analytics Service
- FR-RA-01: Real-time KPI dashboards (premium written, claims paid, loss ratio)
- FR-RA-02: Actuarial reports: loss development, IBNR, frequency/severity
- FR-RA-03: Broker production reports
- FR-RA-04: Regulatory filings: NAIC annual statement data, state-specific formats
- FR-RA-05: Ad-hoc query interface for analysts (SQL-over-Kafka via Kafka Streams)
- FR-RA-06: Reinsurance bordereau export
- FR-RA-07: Scheduled report generation and email delivery

#### 5.11 Portals
- FR-CP-01: Customer Portal (React) — quote, buy, view policy, file claim, pay bill, view documents
- FR-BP-01: Broker Portal (Angular) — client management, quoting, commission, territory map
- FR-AP-01: Admin Portal — claims workbench, underwriting workbench, user/role management, audit log viewer, system health

---

### 6. Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| **Performance** | Policy quote API: p99 < 500ms; Claims FNOL: p99 < 1s; Fraud score: p99 < 200ms |
| **Throughput** | 10,000 concurrent users; 500 TPS peak for payment processing |
| **Availability** | 99.95% uptime for Tier-0 services (Policy, Payment, Claims) |
| **Data Durability** | Zero data loss (RPO = 0 for financial transactions) |
| **Recovery** | RTO < 4 hours for regional failover; RPO < 15 minutes |
| **Scalability** | Horizontal auto-scaling to 10x normal load within 5 minutes |
| **Security** | Zero PII in logs; TLS 1.3 everywhere; RBAC enforced at gateway + service |
| **Compliance** | Full audit trail for all state transitions; 7-year log retention |
| **Maintainability** | Code coverage > 80%; SonarQube quality gate pass required for merge |
| **Portability** | All services containerized; runnable on GCP or Azure without code changes |

---

### 7. Scalability Requirements

- **Horizontal scaling**: All services stateless (state in Redis/PostgreSQL); K8s HPA on CPU + custom metrics
- **Database scaling**: Read replicas for all Tier-0 services; sharding strategy for Claims (by year + line of business)
- **Kafka scaling**: Partition count set 3x expected consumer count; dynamic partition reassignment
- **Cache scaling**: Redis Cluster (consistent hashing across 6+ nodes); read replicas per cluster
- **CDN**: Static portal assets served via GCP CDN; API responses with `Cache-Control` headers
- **Async offload**: All non-critical paths (document generation, notification sending, fraud scoring) are async via Kafka
- **Batch isolation**: Renewal batch, regulatory report generation run in separate K8s job namespaces to avoid noisy-neighbor

---

### 8. Availability and Reliability Requirements

- **Multi-AZ deployment**: All Tier-0 services deployed across 3 AZs minimum in GCP us-central1
- **Circuit Breakers**: Resilience4j on all inter-service HTTP calls; fallback to cached data or graceful degradation
- **Bulkhead isolation**: Thread pool isolation per downstream dependency (Stripe, DocuSign, Jumio)
- **Retry with backoff**: Exponential backoff + jitter on all transient failures; max 3 retries
- **Health checks**: Liveness + readiness probes on all pods; dependent service health included in readiness
- **Dead Letter Queues**: All Kafka consumers have DLQ; DLQ monitoring with alerting
- **Idempotency**: All payment and state-change operations idempotent (idempotency key in header)
- **Outbox Pattern**: All services that produce Kafka events use transactional outbox to prevent event loss

---

### 9. Security Requirements

- **Identity**: Keycloak as IdP; OAuth2 PKCE for browser clients; client_credentials for service-to-service
- **AuthN/AuthZ**: JWT (RS256) validated at API Gateway (Kong); RBAC enforced at service layer via Spring Security
- **Network**: All inter-service traffic via Istio mTLS; no plaintext intra-cluster communication
- **Secrets**: HashiCorp Vault; dynamic PostgreSQL credentials (30-day TTL auto-rotation); K8s auth backend
- **PII protection**: AES-256 encryption at rest for SSN, DOB, financial account numbers; column-level encryption via Spring Data
- **PCI-DSS**: No card PANs stored; Stripe tokenization; quarterly PCI scan
- **SAST/DAST**: SonarQube in CI pipeline; OWASP ZAP in staging pipeline
- **Dependency scanning**: Dependabot + Snyk in CI; block builds with critical CVEs
- **Audit logging**: Every API call logged with user, timestamp, IP, resource, action — immutable append-only log
- **Data masking**: PII masked in non-production environments; synthetic data for dev/test

---

### 10. Compliance and Regulatory Requirements

| Regulation | Requirement | Implementation |
|------------|-------------|----------------|
| **GDPR** | Right to erasure, data export, consent management | Customer Service anonymization API; consent flags in profile |
| **SOX** | Financial transaction controls, 4-eyes approval | Workflow Service approval steps; Audit Service immutability |
| **NAIC Model Laws** | State filing compliance, rate justification | Rate table versioning; filing report generation |
| **State DOI** | 50-state variation in rates, forms, coverage | Reference Data Service with state-specific rate tables and forms |
| **HIPAA-adjacent** | Health information in health insurance line | Separate encrypted schema; access audit; BAA with vendors |
| **OFAC** | Sanctions screening on customers and claimants | Real-time OFAC API call at onboarding and FNOL |
| **PCI-DSS** | Payment card data protection | Stripe tokenization; no PAN storage; quarterly ASV scan |
| **CCPA** | California consumer privacy rights | GDPR-equivalent tooling; CA-resident flag for additional rights |

---

### 11. Data Management Strategy

- **Database per service**: Each microservice owns its own PostgreSQL schema/database — no cross-service DB queries
- **Event-driven data sharing**: Services share data via Kafka events, not direct DB access
- **CDC via Debezium**: Changes in Policy and Customer DBs replicated to Analytics DB via Change Data Capture
- **CQRS**: Policy Service writes to event store; Policy Read Service maintains a denormalized read model
- **Data retention**: Transactional data 7 years; audit logs 10 years; soft delete with `deleted_at` timestamp
- **Archival**: Cold data (>2 years) archived to GCS Nearline via scheduled jobs; queryable via BigQuery
- **PII segregation**: PII stored in encrypted columns; pseudonymized in analytics schemas
- **Schema versioning**: Flyway for all schema migrations; backward-compatible changes only in production

---

### 12. Observability and Monitoring Requirements

**Three Pillars**:

1. **Logging**: Structured JSON logs (logback); correlation ID (`X-Correlation-ID`) propagated across all hops; ELK Stack (Elasticsearch, Logstash, Kibana); PII scrubbed in Logstash pipeline; 30-day hot retention, 1-year cold archive

2. **Metrics**: Spring Actuator → Prometheus scrape → Grafana dashboards; per-service SLI/SLO dashboards; business metrics (policies issued/hour, claims filed/hour, fraud rate); PagerDuty alerts on SLO breach

3. **Distributed Tracing**: OpenTelemetry SDK in all services; trace context in HTTP headers + Kafka message headers; Jaeger backend; trace sampling 10% normal, 100% on error

**Additional**:
- Kafka consumer lag monitoring (Burrow)
- Redis hit ratio and memory pressure alerts
- PostgreSQL slow query log → Prometheus → alert on queries > 1s
- K8s resource utilization dashboards (CPU, memory, network per namespace)
- Synthetic monitoring: canary transactions every 5 minutes testing critical flows
- On-call runbooks linked from Grafana alert panels

---

### 13. Disaster Recovery and Business Continuity

| Tier | Services | RTO | RPO | Strategy |
|------|----------|-----|-----|----------|
| Tier 0 | Policy, Payment, Claims, Customer | 4 hours | 0 (zero data loss) | Active-passive cross-region; PostgreSQL streaming replication to Azure East US |
| Tier 1 | Billing, Premium Calc, Fraud Detection | 8 hours | 15 minutes | Warm standby; restore from Kafka replay |
| Tier 2 | Analytics, Notifications, Documents | 24 hours | 1 hour | Cold standby; restore from GCS backups |

**DR Mechanisms**:
- PostgreSQL WAL streaming to Azure region (cross-cloud DR)
- Kafka topic replication via MirrorMaker 2 to DR cluster
- GCS bucket replication to Azure Blob Storage for documents
- Quarterly DR failover exercises — automated runbook via Terraform + Ansible
- Chaos engineering (Chaos Monkey for K8s) monthly gamedays

---

### 14. Deployment Strategy

- **CI/CD**: Jenkins pipeline → SonarQube gate → unit/integration tests → Docker build → Artifactory push → Helm chart deploy to GKE
- **Environment promotion**: dev → staging → production (via GitOps with ArgoCD)
- **Blue-Green**: Used for major releases involving DB schema changes (Policy, Payment services)
- **Canary**: Used for all feature releases — Istio VirtualService weight splitting (5% → 25% → 100%)
- **Feature flags**: LaunchDarkly for runtime feature toggles; no code deployments for business rule changes
- **Rollback**: Automated rollback if p99 latency > 2x baseline or error rate > 1% during canary phase
- **Infrastructure as Code**: All K8s manifests via Kustomize; infrastructure via Terraform; stored in Git

---

### 15. Risks and Mitigation Strategies

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Distributed transaction complexity | High | High | SAGA pattern with orchestration; Workflow Service as central coordinator |
| Kafka consumer lag causing stale fraud scores | Medium | High | Consumer lag SLA alerts; dedicated fraud topic with high-priority consumer group |
| PostgreSQL connection exhaustion under load | Medium | High | PgBouncer connection pooling; HikariCP at service level; load test at 3x peak |
| Regulatory non-compliance (state-specific) | Medium | Critical | Reference Data Service with state validation; legal review of every rate table change |
| Data breach / PII exposure | Low | Critical | Column-level encryption; Vault secrets; SAST/DAST in every pipeline |
| Vendor lock-in (Stripe, DocuSign) | Low | Medium | Anti-corruption layer (adapter pattern) for every external integration |
| Cascading failures across services | Medium | High | Circuit breakers + bulkheads; timeout budgets; graceful degradation |
| Event schema breaking changes | Medium | Medium | Schema Registry with backward/forward compatibility enforcement |

---

### 16. Success Metrics / KPIs

**Business KPIs**:
- Policies issued per day (target: 5,000)
- Quote-to-bind conversion rate (target: > 35%)
- Claims straight-through processing rate (target: > 60%)
- Fraud loss ratio reduction (target: -15% YoY)
- Broker Net Promoter Score (target: > 50)

**Technical KPIs**:
- Service availability (target: 99.95%)
- Mean time to detect (MTTD) (target: < 5 minutes)
- Mean time to recover (MTTR) (target: < 30 minutes)
- Deployment frequency (target: multiple per day per service)
- Change failure rate (target: < 2%)
- Lead time for change (target: < 1 day)

---

## PART 2 — HIGH-LEVEL ARCHITECTURE (HLD)

---

### 1. High-Level System Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         External Clients                                  │
│   [Customer Portal/React]  [Broker Portal/Angular]  [Admin Portal/React] │
│   [Mobile App iOS/Android]  [Third-Party APIs]  [Broker External APIs]   │
└────────────────────────────────┬─────────────────────────────────────────┘
                                 │ HTTPS / WSS
                    ┌────────────▼─────────────┐
                    │    CDN (GCP Cloud CDN)    │
                    │  Static assets + caching  │
                    └────────────┬─────────────┘
                                 │
                    ┌────────────▼─────────────┐
                    │   API Gateway (Kong)      │
                    │  Rate Limiting · JWT Auth │
                    │  Routing · Load Balancing │
                    │  SSL Termination · CORS   │
                    └────┬────────┬────────┬───┘
                         │        │        │
              ┌──────────▼──┐ ┌───▼────┐ ┌▼──────────┐
              │ Customer BFF│ │Broker  │ │ Admin BFF │
              │  (REST +    │ │  BFF   │ │ (REST +   │
              │  GraphQL)   │ │(GraphQL│ │ GraphQL)  │
              └──────┬──────┘ └───┬────┘ └─────┬─────┘
                     │            │             │
        ┌────────────▼────────────▼─────────────▼──────────────┐
        │                 Istio Service Mesh                     │
        │              (mTLS · Traffic Management)               │
        └──────────────────────────┬────────────────────────────┘
                                   │
        ┌──────────────────────────▼────────────────────────────┐
        │               Domain Microservices                      │
        │                                                         │
        │  [Customer]  [Policy]  [Premium Calc]  [Claims]        │
        │  [Payment]   [Billing] [Document]      [Notification]  │
        │  [Fraud Det] [Analytics] [Broker]      [Reference Data]│
        │  [Workflow Orchestrator]  [Audit]                      │
        └────────┬──────────────────────────┬────────────────────┘
                 │                          │
        ┌────────▼─────┐          ┌─────────▼──────────┐
        │  PostgreSQL   │          │   Apache Kafka      │
        │  (per-service)│          │   (Event Bus)       │
        │  + PgBouncer  │          │   + Schema Registry │
        └────────┬──────┘          └─────────┬──────────┘
                 │                            │
        ┌────────▼──────┐          ┌──────────▼─────────┐
        │  Redis Cluster│          │  Analytics DB       │
        │  (Caching)    │          │  (PostgreSQL OLAP   │
        │               │          │  + Kafka Streams)   │
        └───────────────┘          └────────────────────┘

        Observability: [Prometheus] [Grafana] [Jaeger] [ELK Stack]
        Security:      [Keycloak]  [HashiCorp Vault]  [Istio mTLS]
        Infrastructure:[GKE]       [Terraform]         [ArgoCD]
```

---

### 2. Microservice Boundaries

#### 2.1 Core Domain Services

| Service | Responsibility | DB | APIs Exposed | Events Published | Events Consumed |
|---------|----------------|-----|--------------|-----------------|-----------------|
| **customer-service** | Customer registration, KYC, profile, GDPR | PostgreSQL | POST /customers, GET /customers/{id}, PUT /customers/{id}/profile, POST /customers/{id}/kyc | `customer.registered`, `customer.kyc-completed`, `customer.profile-updated`, `customer.suspended` | `payment.failed` (suspend customer) |
| **policy-service** | Quote, bind, issue, endorse, cancel, renew | PostgreSQL (event store + projection) | POST /quotes, POST /policies, PUT /policies/{id}/endorse, DELETE /policies/{id}/cancel | `policy.quoted`, `policy.bound`, `policy.issued`, `policy.endorsed`, `policy.cancelled`, `policy.renewed`, `policy.lapsed` | `payment.premium-received`, `customer.kyc-completed`, `claim.coverage-verified` |
| **premium-calc-service** | Rating engine, rate tables, discount/surcharge | PostgreSQL | POST /calculate, GET /rate-tables, PUT /rate-tables/{id} | `premium.calculated`, `rate-table.updated` | `policy.quoted` (trigger recalc on endorsement) |
| **claims-service** | FNOL, coverage verification, assignment, reserves, payment auth | PostgreSQL | POST /claims, GET /claims/{id}, PUT /claims/{id}/assign, POST /claims/{id}/reserve | `claim.filed`, `claim.coverage-verified`, `claim.assigned`, `claim.reserve-set`, `claim.approved`, `claim.closed`, `claim.reopened` | `fraud.score-returned`, `policy.issued`, `payment.claim-paid` |
| **payment-service** | Premium collection, claim disbursement, refunds, reconciliation | PostgreSQL | POST /payments, POST /payments/{id}/refund, GET /payments/{id}/status | `payment.premium-received`, `payment.claim-paid`, `payment.refunded`, `payment.failed` | `claim.approved`, `billing.invoice-due`, `policy.cancelled` |
| **billing-service** | Invoice generation, installments, commission calculation | PostgreSQL | GET /billing/{policyId}/invoices, POST /billing/{policyId}/plans | `billing.invoice-generated`, `billing.invoice-due`, `billing.overdue`, `commission.calculated` | `policy.issued`, `policy.endorsed`, `payment.premium-received` |
| **document-service** | PDF generation, GCS storage, e-sign, virus scan | PostgreSQL (metadata) + GCS | POST /documents/generate, GET /documents/{id}, POST /documents/{id}/sign | `document.generated`, `document.signed`, `document.stored` | `policy.issued`, `claim.approved`, `billing.invoice-generated` |
| **notification-service** | Email, SMS, push, in-app via WebSocket | PostgreSQL (preferences + delivery log) | GET /notifications/preferences, PUT /notifications/preferences | `notification.sent`, `notification.failed` | `policy.*`, `claim.*`, `payment.*`, `billing.overdue` |
| **fraud-detection-service** | Real-time scoring, velocity checks, SIU referral | PostgreSQL + Redis (velocity counters) | POST /fraud/score, POST /fraud/cases | `fraud.score-returned`, `fraud.case-opened` | `claim.filed` |
| **broker-service** | Broker profile, appointment, territory, commission | PostgreSQL + PostGIS | GET /brokers/{id}, POST /brokers/{id}/clients, GET /brokers/territory?lat=&lon= | `broker.appointed`, `broker.commission-paid` | `policy.issued`, `commission.calculated` |
| **reference-data-service** | State rules, product definitions, rate tables, jurisdictions | PostgreSQL | GET /products, GET /states/{code}/rules, GET /lines-of-business | `reference-data.updated` | *(none — source of truth)* |
| **analytics-service** | Kafka Streams aggregation, OLAP queries, regulatory reports | PostgreSQL (OLAP schema) | GET /analytics/loss-ratio, GET /analytics/broker-production, GET /reports/naic | *(none — read-only)* | All domain events (consumer group: analytics) |
| **audit-service** | Immutable audit log, vector clock ordering, integrity checks | PostgreSQL (append-only) | GET /audit/{entityType}/{entityId} | *(none)* | All domain events (consumer group: audit) |
| **workflow-orchestrator** | SAGA orchestration for distributed transactions | PostgreSQL | POST /workflows/{type}/start, GET /workflows/{id}/status | `saga.started`, `saga.step-completed`, `saga.compensating`, `saga.completed`, `saga.failed` | All saga reply events from domain services |

#### 2.2 BFF Services

| BFF | Consumers | Pattern | Aggregates From |
|-----|-----------|---------|-----------------|
| **customer-bff** | Customer Portal (React), Mobile App | REST + GraphQL | customer-service, policy-service, claims-service, billing-service, payment-service |
| **broker-bff** | Broker Portal (Angular) | REST + GraphQL | broker-service, policy-service, customer-service, commission data from billing |
| **admin-bff** | Admin Portal (React) | REST + GraphQL | All services — system-wide view |

---

### 3. Event-Driven Architecture

#### 3.1 Key Event Flows

**Flow 1 — Policy Issuance SAGA**
```
Customer Portal → API Gateway → Policy Service
   → publishes: policy.bind-requested
   → Workflow Orchestrator picks up
   → Step 1: Premium Calc Service calculates final premium
   → Step 2: Payment Service charges premium
   → Step 3: Policy Service issues policy
   → Step 4: Document Service generates policy document
   → Step 5: Notification Service sends welcome email
   → Step 6: Billing Service creates billing account
   → SAGA complete → policy.issued published
```

**Flow 2 — Claims FNOL to Payment SAGA**
```
FNOL submitted → claim.filed →
   → Fraud Detection: real-time score → fraud.score-returned
   → Claims Service: coverage verification → claim.coverage-verified
   → Workflow Orchestrator: reserve establishment
   → Adjuster Assignment → claim.assigned
   → Claim Approval → claim.approved
   → Payment Service: disbursement → payment.claim-paid
   → Document Service: EOB document → document.generated
   → Notification Service: settlement notice → notification.sent
```

**Flow 3 — CDC for Analytics**
```
Policy DB → Debezium → Kafka topic: cdc.policy-service.policies →
   → Analytics Service (Kafka Streams) → aggregated metrics →
   → Analytics DB → Grafana dashboards
```

#### 3.2 Outbox Pattern Implementation
Every service that publishes events uses the **transactional outbox**:
1. Business transaction: write domain state + write to `outbox` table in same DB transaction
2. Debezium CDC monitors `outbox` table
3. Debezium publishes to Kafka — guarantees exactly-once with idempotent producer
4. Service marks outbox record as `processed`

#### 3.3 Dead Letter Queues
- Every consumer topic has a corresponding DLQ: `{topic-name}.DLT`
- On 3 consecutive processing failures, message routed to DLT
- DLT consumer: alert ops, store message in audit DB with failure context
- Manual replay tool for DLT messages after fix

---

### 4. Database Schema Strategy

| Service | Schema Approach | Reasoning |
|---------|-----------------|-----------|
| **policy-service** | Event Store (events table) + CQRS projection tables | Full temporal history required; Event Sourcing enables audit, replay, temporal queries |
| **claims-service** | Normalized OLTP schema; partitioned by `(year, line_of_business)` | Claim data grows linearly; partitioning enables efficient archival and query isolation |
| **payment-service** | ACID transactional schema with idempotency_key unique constraint | Financial data requires strictest consistency; no eventual consistency acceptable |
| **customer-service** | Normalized schema + encrypted columns (SSN, DOB) | PII requires encryption at column level |
| **audit-service** | Append-only table; no UPDATE/DELETE permissions at DB level | Immutability enforced at database permission level |
| **analytics-service** | Denormalized OLAP schema; materialized views; read replicas | Query performance over write consistency |
| **fraud-detection-service** | Operational tables + Redis for velocity counters (HyperLogLog for cardinality) | Low latency velocity checks; approximate counting acceptable for fraud signals |

**Sharding Strategy (Claims)**:
- Shard key: `claim_year || line_of_business`
- PostgreSQL range partitioning by `date_of_loss` year
- Horizontal sharding (future): consistent hashing on `claim_id` across DB nodes

**Connection Pooling**: PgBouncer in transaction-mode pooling between K8s pods and PostgreSQL; HikariCP within each Spring Boot service (min=5, max=20 per pod)

**Read Replicas**:
- Policy Service: 2 read replicas for policy search / broker portfolio queries
- Claims Service: 1 read replica for analytics queries
- Analytics Service: All queries on read replica only

---

### 5. Kafka Topic Design

#### Topic Naming Convention
```
{domain}.{aggregate}.{event-type}
```

#### Topic Catalog

| Topic | Producer | Consumer(s) | Partitions | Retention | Key Strategy |
|-------|----------|-------------|------------|-----------|--------------|
| `customer.customers.registered` | customer-service | workflow-orchestrator, notification-service, audit-service | 12 | 7 days | customer_id |
| `customer.customers.kyc-completed` | customer-service | policy-service, audit-service | 12 | 7 days | customer_id |
| `customer.customers.suspended` | customer-service | policy-service, billing-service, audit-service | 6 | 7 days | customer_id |
| `policy.policies.quoted` | policy-service | premium-calc-service, audit-service | 24 | 7 days | quote_id |
| `policy.policies.bound` | policy-service | workflow-orchestrator, audit-service | 24 | 7 days | policy_id |
| `policy.policies.issued` | policy-service | billing-service, document-service, notification-service, broker-service, audit-service, analytics-service | 24 | 30 days | policy_id |
| `policy.policies.endorsed` | policy-service | billing-service, document-service, notification-service, audit-service | 12 | 30 days | policy_id |
| `policy.policies.cancelled` | policy-service | billing-service, payment-service, notification-service, audit-service | 12 | 30 days | policy_id |
| `policy.policies.renewed` | policy-service | billing-service, document-service, notification-service, audit-service | 12 | 30 days | policy_id |
| `premium.calculations.completed` | premium-calc-service | policy-service, audit-service | 24 | 3 days | policy_id |
| `claims.claims.filed` | claims-service | fraud-detection-service, workflow-orchestrator, audit-service, analytics-service | 24 | 30 days | claim_id |
| `claims.claims.coverage-verified` | claims-service | workflow-orchestrator, audit-service | 12 | 7 days | claim_id |
| `claims.claims.assigned` | claims-service | notification-service, audit-service | 12 | 7 days | claim_id |
| `claims.claims.approved` | claims-service | payment-service, document-service, notification-service, workflow-orchestrator, audit-service | 24 | 30 days | claim_id |
| `claims.claims.closed` | claims-service | analytics-service, audit-service, notification-service | 12 | 30 days | claim_id |
| `payment.payments.premium-received` | payment-service | policy-service, billing-service, audit-service, analytics-service | 24 | 30 days | payment_id |
| `payment.payments.claim-paid` | payment-service | claims-service, document-service, notification-service, audit-service | 24 | 30 days | payment_id |
| `payment.payments.failed` | payment-service | billing-service, notification-service, customer-service, audit-service | 12 | 7 days | payment_id |
| `billing.invoices.generated` | billing-service | document-service, notification-service, audit-service | 12 | 30 days | invoice_id |
| `billing.invoices.overdue` | billing-service | notification-service, policy-service, audit-service | 6 | 7 days | invoice_id |
| `billing.commissions.calculated` | billing-service | broker-service, payment-service, audit-service | 6 | 30 days | broker_id |
| `fraud.scores.returned` | fraud-detection-service | claims-service, workflow-orchestrator, audit-service | 24 | 7 days | claim_id |
| `fraud.cases.opened` | fraud-detection-service | notification-service (SIU), audit-service | 6 | 30 days | case_id |
| `document.documents.generated` | document-service | notification-service, audit-service | 12 | 7 days | document_id |
| `document.documents.signed` | document-service | policy-service, claims-service, audit-service | 12 | 7 days | document_id |
| `notification.notifications.sent` | notification-service | audit-service | 6 | 3 days | notification_id |
| `workflow.sagas.step-completed` | workflow-orchestrator | workflow-orchestrator (internal) | 24 | 3 days | saga_id |
| `workflow.sagas.compensating` | workflow-orchestrator | domain services | 24 | 7 days | saga_id |
| `analytics.reports.generated` | analytics-service | notification-service (email report) | 6 | 3 days | report_id |
| `cdc.policy-db.policies` | Debezium | analytics-service | 24 | 7 days | policy_id |
| `cdc.customer-db.customers` | Debezium | analytics-service | 12 | 7 days | customer_id |
| `cdc.claims-db.claims` | Debezium | analytics-service, fraud-detection-service | 24 | 7 days | claim_id |
| `reference.data.updated` | reference-data-service | premium-calc-service, policy-service (cache invalidation) | 3 | 30 days | resource_type |

**Schema Registry**: All events serialized as Avro with Confluent Schema Registry; backward/forward compatibility enforced; schema evolution via field defaults

**Exactly-Once Semantics**: Idempotent producer + transactional consumer for financial events (payment, claims payment)

**Consumer Groups**:
```
cg-workflow-orchestrator     # SAGA coordination
cg-notification              # Notification sending
cg-audit                     # Append-only audit log
cg-analytics                 # Kafka Streams aggregation
cg-fraud-detection           # FNOL scoring
cg-billing-policy            # Billing account management
cg-document-generation       # Document triggers
```

---

### 6. API Gateway Architecture (Kong)

#### Layer Design
```
Internet → GCP Load Balancer (Anycast, SSL termination)
         → Kong API Gateway cluster (3 pods, HPA)
         → BFF Services / Domain Services
```

#### Kong Plugin Stack (ordered)
1. **Rate Limiting**: per-consumer-key limits (customers: 100/min, brokers: 500/min, internal services: unlimited)
2. **JWT Validation**: RS256 JWT from Keycloak; public key fetched from JWKS endpoint; cached 1 hour
3. **OAuth2 Introspection**: For opaque tokens from service accounts
4. **Request Transformer**: Inject `X-Correlation-ID`, `X-User-ID`, `X-User-Roles` headers
5. **Response Transformer**: Strip internal headers before returning to clients
6. **Prometheus Plugin**: Expose metrics for Kong itself (request count, latency, upstream health)
7. **OpenTelemetry Plugin**: Inject W3C Trace Context headers

#### Route Configuration
```yaml
# Customer-facing routes → Customer BFF
/api/v1/customers/** → customer-bff:8080
/api/v1/policies/**  → customer-bff:8080
/api/v1/claims/**    → customer-bff:8080
/api/v1/payments/**  → customer-bff:8080

# Broker routes → Broker BFF
/api/v1/broker/**    → broker-bff:8080

# Admin routes → Admin BFF (requires admin role)
/api/v1/admin/**     → admin-bff:8080

# GraphQL endpoints
/graphql/customer    → customer-bff:4000/graphql
/graphql/broker      → broker-bff:4000/graphql
/graphql/admin       → admin-bff:4000/graphql

# WebSocket (pass-through)
/ws/**               → notification-service:8080
```

#### BFF Pattern (Backend For Frontend)
- **Customer BFF**: Aggregates data for customer journeys; React SPA optimized responses; GraphQL schema for flexible queries
- **Broker BFF**: Bulk operations, bulk quoting, portfolio views; Angular-optimized; geolocation territory queries
- **Admin BFF**: System-wide aggregation; admin-only endpoints; dangerous operations require 2FA re-auth

#### GraphQL Federation
- Each BFF exposes its own GraphQL subgraph
- Apollo Federation Gateway (future Phase 3) federates schemas
- Resolvers call downstream REST APIs; DataLoader for N+1 prevention

---

### 7. Redis Caching Architecture

#### Redis Cluster Layout
```
redis-cluster-tier0:    Policy + Customer data (6 nodes, 3 primary + 3 replica)
redis-cluster-calc:     Premium calculation rate tables (3 nodes)
redis-cluster-fraud:    Velocity counters + fraud signals (3 nodes)
redis-cluster-session:  OAuth2 sessions + API Gateway token cache (3 nodes)
```

#### Cache Catalog

| Cache Key Pattern | Data | TTL | Eviction | Strategy | Invalidation |
|-------------------|------|-----|----------|----------|--------------|
| `customer:{id}:profile` | Customer profile (no PII) | 10 min | LRU | Cache-Aside | `customer.profile-updated` event |
| `policy:{id}:summary` | Policy header (status, limits, deductibles) | 15 min | LRU | Cache-Aside | `policy.*` events |
| `policy:{id}:events` | Event stream projection | 5 min | LRU | Read-Through | `policy.issued` / `endorsed` |
| `quote:{id}` | Quote result | 30 min | LRU | Cache-Aside | TTL expiry only |
| `rate-table:{state}:{lob}:{effective-date}` | Rate factors | 60 min | LFU | Read-Through | `rate-table.updated` event |
| `rate-table:all:index` | Rate table directory | 5 min | LRU | Cache-Aside | `reference-data.updated` |
| `fraud:velocity:customer:{id}:claims:30d` | Claim count (HyperLogLog) | 30 days | None | Write-Through | Expire only |
| `fraud:velocity:address:{hash}:claims:90d` | Claim count per address | 90 days | None | Write-Through | Expire only |
| `fraud:velocity:broker:{id}:claims:7d` | Claim count per broker | 7 days | None | Write-Through | Expire only |
| `fraud:score:{claimId}` | Fraud score result | 24 hours | LRU | Cache-Aside | TTL expiry |
| `session:token:{jti}` | JWT token blacklist (revoked tokens) | JWT exp time | None | Write-Through | Logout event |
| `session:jwks:keycloak` | Keycloak public keys | 1 hour | None | Cache-Aside | Manual invalidation |
| `broker:{id}:portfolio:summary` | Policy count, premium totals | 5 min | LRU | Cache-Aside | `policy.issued`, `policy.cancelled` |
| `broker:territory:{geohash}` | Broker IDs for geohash cell | 30 min | LRU | Cache-Aside | `broker.appointed` |
| `reference:states:{code}:rules` | State insurance rules | 6 hours | LFU | Read-Through | `reference-data.updated` |
| `reference:products:{id}` | Product definition | 1 hour | LFU | Read-Through | `reference-data.updated` |
| `billing:{policyId}:next-invoice` | Next invoice amount + date | 24 hours | LRU | Cache-Aside | `payment.premium-received` |

**Cache Stampede Prevention**:
- Probabilistic early expiration (XFetch algorithm) for hot rate-table keys
- Mutex lock (Redis `SET NX`) when cache miss detected; other requests wait for first thread to populate

**Cache Warming**:
- On service startup: warm rate tables for all active states × lines of business
- After `rate-table.updated` event: proactive cache population before TTL expiry
- Daily batch warm of broker portfolio summaries for top-500 brokers (by policy count)

**Cache Invalidation**:
- Event-driven: notification-service subscribes to domain events, calls cache invalidation endpoint
- Time-based: conservative TTLs ensure staleness bounded by TTL even without explicit invalidation
- Admin API: `/admin/cache/invalidate/{pattern}` for manual cache busting (admin role required)

---

### 8. Kubernetes Deployment Architecture

#### Namespace Layout
```
insurance-platform-prod/
├── ns-gateway          # Kong API Gateway, SSL certs
├── ns-identity         # Keycloak, Vault agent injectors
├── ns-tier0            # Customer, Policy, Payment, Claims services
├── ns-tier1            # Billing, Premium Calc, Fraud Detection, Workflow
├── ns-tier2            # Document, Notification, Broker, Reference Data
├── ns-analytics        # Analytics, Audit services
├── ns-infra            # Kafka, Redis, Debezium (if self-hosted)
├── ns-observability    # Prometheus, Grafana, Jaeger, ELK
└── ns-cicd             # Jenkins agents, ArgoCD
```

#### Per-Service K8s Resources (example: policy-service)
```yaml
Deployment:
  replicas: 3 (min)
  image: gcr.io/eip/policy-service:1.2.3
  resources:
    requests: cpu=500m, memory=512Mi
    limits: cpu=2000m, memory=2Gi
  env:
    - DB_URL from Vault (dynamic credential)
    - KAFKA_BOOTSTRAP from ConfigMap
  readinessProbe: /actuator/health/readiness (delay 30s)
  livenessProbe: /actuator/health/liveness (delay 60s)
  volumeMounts:
    - vault-agent-init (dynamic DB creds)

HPA:
  minReplicas: 3
  maxReplicas: 15
  metrics:
    - cpu: 70%
    - custom: kafka_consumer_lag > 1000

PodDisruptionBudget:
  minAvailable: 2  # Never below 2 pods during rolling update

Service: ClusterIP (internal only)

NetworkPolicy:
  ingress: only from ns-gateway, ns-tier0 (service mesh handles intra-tier)
  egress: Kafka ns-infra, PostgreSQL ns-infra, Keycloak ns-identity, Vault ns-identity

ConfigMap:
  kafka-config: bootstrap servers, consumer group IDs
  app-config: non-sensitive app properties

Vault Agent Sidecar:
  role: policy-service-k8s-role
  secrets: database/creds/policy-service (auto-renewed)
```

#### Istio Configuration
```yaml
# Circuit Breaker via DestinationRule
DestinationRule:
  trafficPolicy:
    outlierDetection:
      consecutiveErrors: 5
      interval: 10s
      baseEjectionTime: 30s
    connectionPool:
      http:
        http1MaxPendingRequests: 100
        http2MaxRequests: 1000

# Canary Release
VirtualService:
  http:
  - match: [uri: /api/v1/policies]
    route:
    - destination: host: policy-service  subset: stable   weight: 95
    - destination: host: policy-service  subset: canary   weight: 5
  retries:
    attempts: 3
    perTryTimeout: 5s
    retryOn: gateway-error,connect-failure,retriable-4xx
```

#### Resource Summary by Tier

| Namespace | Services | Min Replicas | Max Replicas | Storage |
|-----------|----------|--------------|--------------|---------|
| ns-gateway | Kong (2), Kong ingress | 2 | 5 | - |
| ns-identity | Keycloak (3), Vault (3) | 3 | 5 | 50Gi each |
| ns-tier0 | Customer, Policy, Payment, Claims | 3 each | 15 each | - (stateless) |
| ns-tier1 | Billing, Premium-Calc, Fraud, Workflow | 2 each | 10 each | - |
| ns-tier2 | Document, Notification, Broker, Ref-Data | 2 each | 8 each | - |
| ns-analytics | Analytics, Audit | 2 each | 5 each | 500Gi (OLAP DB) |
| ns-infra | Kafka (6), Redis (6), PgBouncer (per svc) | Fixed | Fixed | 2TB Kafka, 100Gi Redis |
| ns-observability | Prometheus, Grafana, Jaeger, ELK | 2 each | 4 each | 1TB logs |

---

## PART 3 — ADDITIONAL RECOMMENDATIONS & MISSING COMPONENTS

### Additional Technologies to Include

| Technology | Purpose | Where |
|------------|---------|--------|
| **Apache Flink** | Stateful stream processing for complex fraud event detection (CEP) | Fraud Detection |
| **Elasticsearch** | Policy full-text search; claim document search | Policy Read Service |
| **PostGIS** | Geospatial broker territory queries | Broker Service |
| **Apache Avro + Schema Registry** | Strongly-typed Kafka event serialization | All Kafka topics |
| **Backstage** | Internal developer portal — service catalog, API docs, ADRs | Developer experience |
| **Terraform** | Infrastructure as Code for GKE, Cloud SQL, Redis, Kafka | All infrastructure |
| **ArgoCD** | GitOps CD for K8s manifest deployment | CI/CD |
| **LaunchDarkly** | Feature flags for safe progressive rollout | All services |
| **Chaos Monkey (Chaos Toolkit)** | Chaos engineering for resilience validation | SRE practice |
| **Swagger/OpenAPI 3.0** | API contract documentation; generated from Spring annotations | All REST APIs |
| **Spring Cloud Config** | Centralized configuration (fallback for non-secret config) | All services |
| **Micrometer** | Metrics facade for Spring services → Prometheus | All services |
| **OpenTelemetry Java Agent** | Zero-code instrumentation for traces | All services |
| **Testcontainers** | Integration testing with real PostgreSQL, Kafka, Redis | All service tests |

### Missing Components Identified

1. **Actuarial Service** — IBNR calculations, loss development factors, exposure reporting — critical for insurance financial accuracy
2. **Reinsurance Service** — bordereau generation, treaty management, cession calculations
3. **Regulatory Filing Service** — automated NAIC annual statement data extraction and state-specific form generation
4. **Product Configuration Service** — self-service product/coverage definition without code changes
5. **Agent Licensing Verification** — real-time check of broker license validity per state (NIPR integration)
6. **Telematics Integration Service** — usage-based insurance data ingestion (for Auto line)
7. **Third-Party Administrator (TPA) Integration** — for outsourced claims administration
8. **Print/Mail Fulfillment Service** — physical policy documents for jurisdictions requiring paper
9. **Developer Portal** — external API access for broker systems integration

### Architecture Decision Records (ADR) Summary

| ADR | Decision | Rationale |
|-----|----------|-----------|
| ADR-001 | SAGA over Two-Phase Commit | 2PC creates lock contention across 14 independent DBs; SAGA enables eventual consistency with explicit compensation |
| ADR-002 | Event Sourcing for Policy domain only | Policy needs full temporal history; applying platform-wide adds unnecessary complexity |
| ADR-003 | Kappa over Lambda for analytics | Single streaming pipeline eliminates dual code-path maintenance; Kafka replay covers reprocessing |
| ADR-004 | GraphQL at BFF layer, not service layer | Prevents N+1 at service level; BFF is the composition layer |

---

## PART 4 — IMPLEMENTATION SEQUENCING

### Phase 1 — Foundation (Months 1-3)
1. GKE cluster + Istio + namespaces + network policies
2. Keycloak + Vault + PostgreSQL clusters + Kafka + Redis
3. Customer Service + Premium Calc + Policy Service (happy path)
4. Billing + Payment (Stripe integration)
5. API Gateway (Kong) + Customer BFF + Customer Portal (React MVP)
6. Notification Service (email only)
7. Audit Service
8. Full observability stack

**Exit Criteria**: Customer registers → KYC passes → policy quoted → bound → issued → invoice → payment → notification → audit trail

### Phase 2 — Claims and Fraud (Months 4-6)
1. Document Service (GCS + PDF + DocuSign)
2. Claims Service (FNOL → assignment → reserve → approval)
3. Fraud Detection (rule-based)
4. Workflow Orchestrator (SAGA engine)
5. Broker Service + Broker Portal (Angular)
6. Admin Portal — claims workbench

**Exit Criteria**: End-to-end claims flow with fraud scoring, adjuster assignment, reserve, claim payment

### Phase 3 — Advanced Capabilities (Months 7-12)
1. ML fraud model (TensorFlow Serving)
2. Policy Read Service + Elasticsearch (CQRS full)
3. Analytics Service (Kafka Streams pipelines)
4. Renewal and endorsement workflows
5. Multi-channel notifications (SMS, push, WebSocket)
6. GraphQL Federation gateway
7. Blue-Green + Canary automation
8. First chaos engineering gameday

### Phase 4 — Enterprise Hardening (Months 13-18)
1. Multi-tenancy (row-level security, scoped Keycloak realms)
2. GDPR tooling (erasure workflow, consent audit)
3. SOX controls (4-eyes approval above financial thresholds)
4. Regulatory reporting automation (NAIC, state DOIs)
5. External penetration test + remediation
6. Full DR failover test + runbook automation
7. Actuarial Service and Reinsurance Service

---

## Critical File Paths for Implementation

| Path | Purpose |
|------|---------|
| `/services/{service-name}/src/main/java/com/eip/{domain}/` | Per-service Java source root |
| `/services/policy-service/src/main/java/com/eip/policy/domain/` | Event sourcing aggregate, event store, SAGA participant |
| `/services/workflow-orchestrator/src/main/java/com/eip/workflow/saga/` | SAGA engine — most architecturally critical component |
| `/infrastructure/kubernetes/base/` | Kustomize base manifests (namespaces, RBAC, Istio, network policies) |
| `/infrastructure/kafka/schemas/` | Avro schemas for all 42+ domain event types |
| `/infrastructure/terraform/` | GKE, Cloud SQL, Redis, Kafka Terraform modules |
| `/docs/architecture/ADRs/` | Architecture Decision Records |
| `/docs/architecture/HLD.md` | This document (maintained as living document) |

---

## Verification Checklist

- [ ] End-to-end happy path: customer registration → policy issuance → premium collection
- [ ] SAGA compensation: policy bind fails at payment → verify policy rolled back
- [ ] Circuit breaker: simulate Stripe outage → verify graceful degradation
- [ ] Cache stampede: flush rate-table cache under load → verify no thundering herd
- [ ] Fraud scoring: FNOL with fraud signals → verify score returned < 200ms
- [ ] Audit trail: complete trace of policy issuance through audit service
- [ ] DR failover: regional failover test with < 4-hour RTO verified
- [ ] Security: JWT with wrong role denied at gateway and service layer
- [ ] GDPR: customer erasure request removes PII without breaking audit log
- [ ] Performance: p99 < 500ms for quote API under 1000 concurrent users
