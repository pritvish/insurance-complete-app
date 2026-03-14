CREATE TABLE claims (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id                VARCHAR(30) UNIQUE NOT NULL,
    policy_id               VARCHAR(50) NOT NULL,
    customer_id             VARCHAR(50) NOT NULL,
    assigned_adjuster_id    VARCHAR(50),
    line_of_business        VARCHAR(30) NOT NULL,
    status                  VARCHAR(30) NOT NULL DEFAULT 'FNOL_RECEIVED',
    date_of_loss            DATE NOT NULL,
    date_reported           DATE NOT NULL DEFAULT CURRENT_DATE,
    fnol_submitted_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description             TEXT NOT NULL,
    claim_amount            DECIMAL(12,2),
    reserve_amount          DECIMAL(12,2),
    paid_amount             DECIMAL(12,2) DEFAULT 0,
    coverage_verified       BOOLEAN DEFAULT FALSE,
    fraud_score             INTEGER,
    fraud_score_returned_at TIMESTAMPTZ,
    litigation_flagged      BOOLEAN DEFAULT FALSE,
    subrogation_applicable  BOOLEAN DEFAULT FALSE,
    notes                   TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_claims_policy ON claims(policy_id);
CREATE INDEX idx_claims_customer ON claims(customer_id);
CREATE INDEX idx_claims_status ON claims(status);
CREATE INDEX idx_claims_adjuster ON claims(assigned_adjuster_id) WHERE assigned_adjuster_id IS NOT NULL;
CREATE INDEX idx_claims_date_of_loss ON claims(date_of_loss);

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
