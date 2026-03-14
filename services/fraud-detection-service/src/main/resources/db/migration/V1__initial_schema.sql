CREATE TABLE fraud_scores (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id        VARCHAR(30) NOT NULL,
    customer_id     VARCHAR(50) NOT NULL,
    policy_id       VARCHAR(50),
    score           INTEGER NOT NULL CHECK (score >= 0 AND score <= 100),
    risk_level      VARCHAR(20) NOT NULL,
    signals         TEXT,
    scored_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    model_version   VARCHAR(20)
);
CREATE INDEX idx_fraud_scores_claim ON fraud_scores(claim_id);
CREATE INDEX idx_fraud_scores_customer ON fraud_scores(customer_id);

CREATE TABLE fraud_cases (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id         VARCHAR(30) UNIQUE NOT NULL,
    claim_id        VARCHAR(30) NOT NULL,
    customer_id     VARCHAR(50) NOT NULL,
    case_status     VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    severity        VARCHAR(20),
    referred_to_siu BOOLEAN DEFAULT FALSE,
    opened_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    closed_at       TIMESTAMPTZ,
    investigator_id VARCHAR(50),
    findings        TEXT
);
CREATE INDEX idx_fraud_cases_claim ON fraud_cases(claim_id);
CREATE INDEX idx_fraud_cases_status ON fraud_cases(case_status);
