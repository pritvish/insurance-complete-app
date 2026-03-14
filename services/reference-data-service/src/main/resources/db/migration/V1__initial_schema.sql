CREATE TABLE IF NOT EXISTS insurance_products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_code        VARCHAR(50)  NOT NULL UNIQUE,
    product_name        VARCHAR(200) NOT NULL,
    line_of_business    VARCHAR(50)  NOT NULL,
    description         TEXT,
    min_coverage_limit  DECIMAL(15,2),
    max_coverage_limit  DECIMAL(15,2),
    min_deductible      DECIMAL(10,2),
    max_deductible      DECIMAL(10,2),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS state_rules (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    state_code                  VARCHAR(5)   NOT NULL,
    state_name                  VARCHAR(100) NOT NULL,
    line_of_business            VARCHAR(50)  NOT NULL,
    min_liability_limit         DECIMAL(15,2),
    mandatory_coverage_types    TEXT,
    regulatory_surcharge_rate   DECIMAL(5,4) NOT NULL DEFAULT 0,
    no_fault_state              BOOLEAN NOT NULL DEFAULT FALSE,
    is_active                   BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (state_code, line_of_business)
);

CREATE INDEX idx_product_code ON insurance_products (product_code);
CREATE INDEX idx_product_lob  ON insurance_products (line_of_business);
CREATE INDEX idx_state_code   ON state_rules (state_code);
