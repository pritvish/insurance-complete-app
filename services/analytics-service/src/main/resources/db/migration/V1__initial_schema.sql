-- Analytics read model: denormalized OLAP tables aggregated from Kafka events.
-- These tables are written by Kafka consumers; never modified via business APIs.

CREATE TABLE IF NOT EXISTS policy_metrics (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id            UUID NOT NULL,
    customer_id          UUID,
    broker_id            UUID,
    product_code         VARCHAR(50),
    status               VARCHAR(50),
    premium_amount       DECIMAL(15,2),
    coverage_amount      DECIMAL(15,2),
    issued_at            TIMESTAMPTZ,
    expires_at           TIMESTAMPTZ,
    claim_count          INT NOT NULL DEFAULT 0,
    total_claims_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
    last_event_type      VARCHAR(100),
    last_updated_at      TIMESTAMPTZ,
    version              BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_pm_policy_id    ON policy_metrics (policy_id);
CREATE INDEX idx_pm_customer_id         ON policy_metrics (customer_id);
CREATE INDEX idx_pm_broker_id           ON policy_metrics (broker_id);
CREATE INDEX idx_pm_product_code        ON policy_metrics (product_code);
CREATE INDEX idx_pm_status              ON policy_metrics (status);
CREATE INDEX idx_pm_issued_at           ON policy_metrics (issued_at DESC);

-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS claim_metrics (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id               UUID NOT NULL,
    policy_id              UUID,
    customer_id            UUID,
    claim_type             VARCHAR(50),
    status                 VARCHAR(50),
    filed_amount           DECIMAL(15,2),
    approved_amount        DECIMAL(15,2),
    filed_at               TIMESTAMPTZ,
    settled_at             TIMESTAMPTZ,
    processing_days_count  INT,
    fraud_score            DECIMAL(5,2),
    is_fraudulent          BOOLEAN NOT NULL DEFAULT FALSE,
    last_event_type        VARCHAR(100),
    last_updated_at        TIMESTAMPTZ,
    version                BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_cm_claim_id     ON claim_metrics (claim_id);
CREATE INDEX idx_cm_policy_id           ON claim_metrics (policy_id);
CREATE INDEX idx_cm_customer_id         ON claim_metrics (customer_id);
CREATE INDEX idx_cm_status              ON claim_metrics (status);
CREATE INDEX idx_cm_filed_at            ON claim_metrics (filed_at DESC);

-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS broker_metrics (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    broker_id                UUID NOT NULL,
    broker_name              VARCHAR(200),
    total_policies           INT NOT NULL DEFAULT 0,
    active_policies          INT NOT NULL DEFAULT 0,
    total_premium_collected  DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_claims_count       INT NOT NULL DEFAULT 0,
    claims_ratio             DECIMAL(5,4) NOT NULL DEFAULT 0,
    last_event_type          VARCHAR(100),
    last_updated_at          TIMESTAMPTZ,
    version                  BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_bm_broker_id        ON broker_metrics (broker_id);
CREATE INDEX idx_bm_total_premium           ON broker_metrics (total_premium_collected DESC);
