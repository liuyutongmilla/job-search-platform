--
--

CREATE TABLE jobs (
    job_id          BIGSERIAL     PRIMARY KEY,
    title           VARCHAR(200)  NOT NULL,
    company         VARCHAR(200)  NOT NULL,
    city            VARCHAR(100)  NOT NULL,
    min_salary      INTEGER,
    max_salary      INTEGER,
    required_years  INTEGER,
    description     TEXT          NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    posted_at       TIMESTAMP     NOT NULL,

    created_at      TIMESTAMP     NOT NULL,
    created_by      VARCHAR(64)   NOT NULL,
    updated_at      TIMESTAMP,
    updated_by      VARCHAR(64),

    CONSTRAINT chk_jobs_status  CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_jobs_salary  CHECK (min_salary IS NULL OR max_salary IS NULL OR min_salary <= max_salary)
);

CREATE UNIQUE INDEX uq_jobs_title_company_city ON jobs (title, company, city);

CREATE TABLE job_skills (
    job_id  BIGINT       NOT NULL REFERENCES jobs (job_id) ON DELETE CASCADE,
    skill   VARCHAR(100) NOT NULL,
    PRIMARY KEY (job_id, skill)
);

CREATE INDEX idx_jobs_status_city   ON jobs (status, city);
CREATE INDEX idx_jobs_posted_at     ON jobs (posted_at DESC);
CREATE INDEX idx_jobs_status_years  ON jobs (status, required_years);
CREATE INDEX idx_job_skills_lower   ON job_skills (LOWER(skill));

INSERT INTO jobs (title, company, city, min_salary, max_salary, required_years,
                  description, status, posted_at, created_at, created_by)
VALUES
  ('Java 后端工程师', 'Acme 科技', '上海', 25000, 40000, 3,
   '负责核心交易系统的微服务开发与治理，要求熟悉 Spring Cloud 生态。',
   'OPEN', NOW(), NOW(), 'SEED'),
  ('数据平台工程师', 'Acme 科技', '北京', 30000, 50000, 5,
   '负责实时数仓建设，Flink / Kafka 方向。',
   'OPEN', NOW(), NOW(), 'SEED'),
  ('初级后端工程师', 'Beta 网络', '上海', 12000, 18000, 0,
   '参与业务系统开发，有 Java 基础即可，提供带教。',
   'OPEN', NOW(), NOW(), 'SEED');

INSERT INTO job_skills (job_id, skill) VALUES
  (1, 'Java'), (1, 'Spring Boot'), (1, 'Kafka'), (1, 'PostgreSQL'),
  (2, 'Java'), (2, 'Flink'), (2, 'Kafka'),
  (3, 'Java'), (3, 'MySQL');
