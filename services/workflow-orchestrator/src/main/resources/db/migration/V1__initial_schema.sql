-- Saga orchestration tables.
-- saga_instances tracks each business process (one row per saga execution).
-- saga_steps tracks each step within a saga with its status and payload.

CREATE TABLE IF NOT EXISTS saga_instances (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_type      VARCHAR(50)  NOT NULL,
    saga_status    VARCHAR(50)  NOT NULL,
    correlation_id VARCHAR(100) NOT NULL,
    current_step   VARCHAR(100),
    failure_reason TEXT,
    payload        TEXT,
    started_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at   TIMESTAMPTZ,
    version        BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_saga_correlation      ON saga_instances (correlation_id);
CREATE INDEX idx_saga_status           ON saga_instances (saga_status);
CREATE UNIQUE INDEX idx_saga_correlation_type ON saga_instances (correlation_id, saga_type);

-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS saga_steps (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    saga_instance_id UUID         NOT NULL REFERENCES saga_instances (id) ON DELETE CASCADE,
    step_name        VARCHAR(100) NOT NULL,
    step_order       INT          NOT NULL,
    step_status      VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    input_payload    TEXT,
    output_payload   TEXT,
    failure_reason   TEXT,
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    retry_count      INT          NOT NULL DEFAULT 0,
    version          BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_saga_step_instance ON saga_steps (saga_instance_id, step_order);
