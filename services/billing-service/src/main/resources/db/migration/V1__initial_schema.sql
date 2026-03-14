CREATE TABLE billing_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id          VARCHAR(30) UNIQUE NOT NULL,
    policy_id           VARCHAR(50) UNIQUE NOT NULL,
    customer_id         VARCHAR(50) NOT NULL,
    billing_frequency   VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    next_due_date       DATE,
    outstanding_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billing_accounts_policy ON billing_accounts(policy_id);
CREATE INDEX idx_billing_accounts_customer ON billing_accounts(customer_id);

CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id          VARCHAR(30) UNIQUE NOT NULL,
    account_id          VARCHAR(30) NOT NULL,
    policy_id           VARCHAR(50) NOT NULL,
    amount              DECIMAL(12,2) NOT NULL,
    due_date            DATE NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    issued_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at             TIMESTAMPTZ,
    grace_period_ends_at DATE
);

CREATE INDEX idx_invoices_account ON invoices(account_id);
CREATE INDEX idx_invoices_policy ON invoices(policy_id);
CREATE INDEX idx_invoices_status_due ON invoices(status, due_date);
