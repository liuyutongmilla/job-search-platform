CREATE TABLE job_matches (
    match_id       BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    job_id         BIGINT       NOT NULL,
    resume_version INTEGER      NOT NULL,
    score          INTEGER      NOT NULL,
    strengths      JSONB,
    gaps           JSONB,
    explanation    TEXT,
    engine         VARCHAR(20)  NOT NULL,
    computed_at    TIMESTAMP    NOT NULL,

    created_at     TIMESTAMP    NOT NULL,
    created_by     VARCHAR(64)  NOT NULL,
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(64),

    CONSTRAINT chk_matches_engine CHECK (engine IN ('AI', 'RULE_BASED')),
    CONSTRAINT chk_matches_score  CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT uq_matches_user_job_version UNIQUE (user_id, job_id, resume_version)
);

CREATE INDEX idx_matches_user_version_score ON job_matches (user_id, resume_version, score DESC);
CREATE INDEX idx_matches_engine ON job_matches (engine, computed_at) WHERE engine = 'RULE_BASED';
