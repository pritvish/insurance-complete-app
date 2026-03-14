CREATE TABLE payments (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id                  VARCHAR(30) UNIQUE NOT NULL,
    idempotency_key             VARCHAR(100) UNIQUE NOT NULL,
    payment_type                VARCHAR(30) NOT NULL,
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount                      DECIMAL(12,2) NOT NULL,
    currency                    CHAR(3) NOT NULL DEFAULT 'USD',
    policy_id                   VARCHAR(50),
    claim_id                    VARCHAR(30),
    customer_id                 VARCHAR(50),
    broker_id                   VARCHAR(50),
    payment_method              VARCHAR(20) NOT NULL,
    stripe_payment_intent_id    VARCHAR(255),
    stripe_charge_id            VARCHAR(255),
    retry_count                 INTEGER NOT NULL DEFAULT 0,
    next_retry_at               TIMESTAMPTZ,
    failure_reason              TEXT,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at                TIMESTAMPTZ,
    version                     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payments_policy ON payments(policy_id);
CREATE INDEX idx_payments_claim ON payments(claim_id) WHERE claim_id IS NOT NULL;
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_retry ON payments(status, next_retry_at) WHERE status = 'FAILED';

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
