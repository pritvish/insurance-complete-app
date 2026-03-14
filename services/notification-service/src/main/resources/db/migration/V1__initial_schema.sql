CREATE TABLE IF NOT EXISTS notification_preferences (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     VARCHAR(100) NOT NULL UNIQUE,
    email_address   VARCHAR(255),
    phone_number    VARCHAR(30),
    email_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    marketing_opted_in BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notification_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id     VARCHAR(100) NOT NULL UNIQUE,
    customer_id         VARCHAR(100),
    correlation_id      VARCHAR(100),
    trigger_event       VARCHAR(200),
    channel             VARCHAR(20) NOT NULL CHECK (channel IN ('EMAIL','SMS','IN_APP','PUSH')),
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','SENT','DELIVERED','FAILED','SUPPRESSED')),
    recipient           VARCHAR(255),
    subject             VARCHAR(500),
    template_id         VARCHAR(100),
    provider_message_id VARCHAR(200),
    failure_reason      TEXT,
    sent_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_customer    ON notification_logs (customer_id, sent_at DESC);
CREATE INDEX idx_notif_correlation ON notification_logs (correlation_id) WHERE correlation_id IS NOT NULL;
CREATE INDEX idx_notif_status      ON notification_logs (status);
