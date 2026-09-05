-- user 是 PostgreSQL 保留字，所以表名用 app_users
CREATE TABLE app_users (
    user_id         BIGSERIAL     PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(200)  NOT NULL,
    -- ⚠️ 生产环境：phone 属于个人敏感信息，应当加密存储（pgcrypto 或应用层加密），
    -- 并且列表查询接口不返回明文。这里为了教学保持明文，但这是必须偿还的技术债。
    phone           VARCHAR(30),
    city            VARCHAR(100),
    expected_salary INTEGER,

    created_at      TIMESTAMP     NOT NULL,
    created_by      VARCHAR(64)   NOT NULL,
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(64),

    CONSTRAINT uq_app_users_email CHECK (email <> '')
);

CREATE UNIQUE INDEX uq_app_users_email_idx ON app_users (LOWER(email));

CREATE TABLE resumes (
    resume_id         BIGSERIAL     PRIMARY KEY,
    user_id           BIGINT        NOT NULL REFERENCES app_users (user_id) ON DELETE CASCADE,
    file_key          VARCHAR(500)  NOT NULL,
    original_filename VARCHAR(300),
    content_type      VARCHAR(100),
    size_bytes        BIGINT,
    parse_status      VARCHAR(20)   NOT NULL,
    -- 结构化解析结果。JSONB 而非拆表：简历结构会随 prompt 迭代变化，
    -- 早期保留灵活性；需要按字段查询时 JSONB 也能建 GIN 索引。
    parsed_json       JSONB,
    parse_error       VARCHAR(1000),
    version           INTEGER       NOT NULL,
    uploaded_at       TIMESTAMP     NOT NULL,

    created_at        TIMESTAMP     NOT NULL,
    created_by        VARCHAR(64)   NOT NULL,
    updated_at        TIMESTAMP,
    updated_by        VARCHAR(64),

    CONSTRAINT chk_resumes_status CHECK (parse_status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED')),
    CONSTRAINT uq_resumes_user_version UNIQUE (user_id, version)
);

CREATE INDEX idx_resumes_user_version ON resumes (user_id, version DESC);
-- 运维用：快速找出卡在 PENDING/PROCESSING 太久的简历（解析链路是否堵了）
CREATE INDEX idx_resumes_pending ON resumes (parse_status, uploaded_at)
    WHERE parse_status IN ('PENDING', 'PROCESSING');

INSERT INTO app_users (name, email, phone, city, expected_salary, created_at, created_by)
VALUES ('测试用户', 'demo@example.com', '13800000000', '上海', 28000, NOW(), 'SEED');
