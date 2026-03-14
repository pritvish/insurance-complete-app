-- Audit records are APPEND-ONLY. Never update or delete.
-- REVOKE UPDATE, DELETE ON audit_records FROM PUBLIC;

CREATE TABLE IF NOT EXISTS audit_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    correlation_id  VARCHAR(100),
    event_type      VARCHAR(200)  NOT NULL,
    entity_type     VARCHAR(100)  NOT NULL,
    entity_id       VARCHAR(100)  NOT NULL,
    user_id         VARCHAR(100),
    service_source  VARCHAR(100),
    payload         TEXT,
    previous_state  TEXT,
    new_state       TEXT,
    occurred_at     TIMESTAMPTZ   NOT NULL,
    recorded_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    ip_address      VARCHAR(45),
    session_id      VARCHAR(200)
);

-- Primary access patterns
CREATE INDEX idx_audit_entity     ON audit_records (entity_type, entity_id, recorded_at DESC);
CREATE INDEX idx_audit_correlation ON audit_records (correlation_id) WHERE correlation_id IS NOT NULL;
CREATE INDEX idx_audit_user        ON audit_records (user_id, occurred_at DESC) WHERE user_id IS NOT NULL;
CREATE INDEX idx_audit_event_type  ON audit_records (event_type, recorded_at DESC);
