CREATE TABLE IF NOT EXISTS brokers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    broker_id           VARCHAR(100) NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone               VARCHAR(30),
    agency_name         VARCHAR(255),
    license_number      VARCHAR(100) NOT NULL UNIQUE,
    license_state       VARCHAR(5),
    license_expiry_date DATE,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE','SUSPENDED','TERMINATED','PENDING_APPROVAL')),
    commission_rate     DECIMAL(5,4) NOT NULL DEFAULT 0.0750,
    ytd_premium_volume  DECIMAL(15,2) NOT NULL DEFAULT 0,
    ytd_policy_count    INTEGER      NOT NULL DEFAULT 0,
    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_broker_id      ON brokers (broker_id);
CREATE INDEX idx_broker_email   ON brokers (email);
CREATE INDEX idx_broker_license ON brokers (license_number);
CREATE INDEX idx_broker_status  ON brokers (status);
