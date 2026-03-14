-- Customer Service Initial Schema
-- Version: 1

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     VARCHAR(50) UNIQUE NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) UNIQUE NOT NULL,
    phone           VARCHAR(20),
    date_of_birth   DATE NOT NULL,
    ssn_hash        VARCHAR(255) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING_KYC'
                        CHECK (status IN ('PENDING_KYC', 'ACTIVE', 'SUSPENDED', 'CLOSED')),
    kyc_status      VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                        CHECK (kyc_status IN ('PENDING', 'IN_PROGRESS', 'PASSED', 'FAILED')),
    kyc_completed_at            TIMESTAMPTZ,
    kyc_reference               VARCHAR(255),
    ofac_screening_status       VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                                    CHECK (ofac_screening_status IN ('PENDING', 'CLEAR', 'FLAGGED')),
    credit_score                INTEGER,
    consent_given               BOOLEAN NOT NULL DEFAULT FALSE,
    consent_timestamp           TIMESTAMPTZ,
    gdpr_erasure_requested_at   TIMESTAMPTZ,

    -- Embedded address
    street1     VARCHAR(255),
    street2     VARCHAR(255),
    city        VARCHAR(100),
    state       VARCHAR(50),
    zip_code    VARCHAR(20),
    country     VARCHAR(50) DEFAULT 'US',

    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    version     BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_customers_email ON customers(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_customer_id ON customers(customer_id);
CREATE INDEX idx_customers_status ON customers(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_customers_kyc_status ON customers(kyc_status) WHERE deleted_at IS NULL;

-- Outbox table for transactional event publishing (Outbox Pattern)
CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,
    aggregate_id    VARCHAR(255) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at    TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status ON outbox_events(status, created_at) WHERE status = 'PENDING';
