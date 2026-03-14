CREATE TABLE rate_tables (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    table_code          VARCHAR(50) UNIQUE NOT NULL,
    line_of_business    VARCHAR(30) NOT NULL,
    state_code          CHAR(2) NOT NULL,
    effective_date      DATE NOT NULL,
    expiration_date     DATE,
    rate_factors        TEXT NOT NULL,
    base_rate           DECIMAL(8,6) NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_rate_tables_lob_state ON rate_tables(line_of_business, state_code, effective_date DESC);

CREATE TABLE premium_calculations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calculation_id      VARCHAR(100) UNIQUE NOT NULL,
    customer_id         VARCHAR(50) NOT NULL,
    line_of_business    VARCHAR(30) NOT NULL,
    state_code          CHAR(2) NOT NULL,
    coverage_limit      DECIMAL(14,2) NOT NULL,
    deductible          DECIMAL(10,2) NOT NULL,
    base_premium        DECIMAL(12,2) NOT NULL,
    final_premium       DECIMAL(12,2) NOT NULL,
    rate_table_used     VARCHAR(50),
    calculated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_prem_calc_customer ON premium_calculations(customer_id);
