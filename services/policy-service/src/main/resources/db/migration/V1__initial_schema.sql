-- Policy Service Initial Schema (Event Sourcing + CQRS)

-- Event Store: append-only, never update/delete
CREATE TABLE policy_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id       VARCHAR(50) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    event_version   BIGINT NOT NULL,
    payload         TEXT NOT NULL,
    correlation_id  VARCHAR(100),
    causation_id    VARCHAR(100),
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (policy_id, event_version)
);

CREATE INDEX idx_policy_events_policy_id ON policy_events(policy_id);
CREATE INDEX idx_policy_events_type ON policy_events(event_type);

-- CQRS Read Model (projection from events)
CREATE TABLE policy_projections (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id       VARCHAR(50) UNIQUE NOT NULL,
    quote_id        VARCHAR(50),
    customer_id     VARCHAR(50) NOT NULL,
    broker_id       VARCHAR(50),
    line_of_business VARCHAR(30) NOT NULL,
    product_code    VARCHAR(50) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'QUOTED'
                        CHECK (status IN ('QUOTED','BOUND','ISSUED','ENDORSED','CANCELLED','LAPSED','RENEWED','EXPIRED')),
    effective_date  DATE NOT NULL,
    expiration_date DATE NOT NULL,
    premium_amount  DECIMAL(12,2) NOT NULL,
    coverage_limit  DECIMAL(14,2) NOT NULL,
    deductible      DECIMAL(10,2) NOT NULL,
    state_code      CHAR(2) NOT NULL,
    insured_name    VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_policy_proj_customer ON policy_projections(customer_id);
CREATE INDEX idx_policy_proj_broker ON policy_projections(broker_id) WHERE broker_id IS NOT NULL;
CREATE INDEX idx_policy_proj_status ON policy_projections(status);
CREATE INDEX idx_policy_proj_expiration ON policy_projections(expiration_date) WHERE status = 'ISSUED';

-- Outbox for event publishing
CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status ON outbox_events(status, created_at) WHERE status = 'PENDING';
