CREATE TABLE IF NOT EXISTS documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id     VARCHAR(100) NOT NULL UNIQUE,
    document_type   VARCHAR(50)  NOT NULL,
    entity_type     VARCHAR(100),
    entity_id       VARCHAR(100),
    customer_id     VARCHAR(100),
    gcs_bucket      VARCHAR(255),
    gcs_object_key  VARCHAR(500),
    gcs_signed_url_expiry TIMESTAMPTZ,
    file_size_bytes BIGINT,
    content_type    VARCHAR(100),
    correlation_id  VARCHAR(100),
    signed_at       TIMESTAMPTZ,
    signed_by       VARCHAR(200),
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_doc_entity   ON documents (entity_type, entity_id);
CREATE INDEX idx_doc_customer ON documents (customer_id);
CREATE INDEX idx_doc_type     ON documents (document_type);
