CREATE TABLE job_matches (
    match_id       BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    job_id         BIGINT       NOT NULL,
    -- 简历版本号：简历一改，旧分数自动失效（因为唯一键里带了它）
    resume_version INTEGER      NOT NULL,
    score          INTEGER      NOT NULL,
    strengths      JSONB,
    gaps           JSONB,
    explanation    TEXT,
    -- AI / RULE_BASED。落库是刻意的：降级率是核心 SLO 指标，
    -- 而且 AI 恢复后可以定向重算 engine = 'RULE_BASED' 的记录
    engine         VARCHAR(20)  NOT NULL,
    computed_at    TIMESTAMP    NOT NULL,

    created_at     TIMESTAMP    NOT NULL,
    created_by     VARCHAR(64)  NOT NULL,
    updated_at     TIMESTAMP,
    updated_by     VARCHAR(64),

    -- ⚠️ 注意这里【没有】外键指向 app_users / jobs。
    -- 那两张表在别的服务的【别的数据库】里，跨库外键做不到，
    -- 而且微服务本来就不该让数据库层耦合。
    -- 引用完整性由应用层保证（Feign 调用时上游会返回 404）。
    -- 代价是可能存在"指向已删除职位"的孤儿记录 —— 靠定时清理任务处理。
    CONSTRAINT chk_matches_engine CHECK (engine IN ('AI', 'RULE_BASED')),
    -- LLM 偶尔会返回 105 或 -3 这种超范围分数，数据库这道 CHECK 是最后防线；
    -- 应用层的 clampScore() 是第一道防线，两道都要有
    CONSTRAINT chk_matches_score  CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT uq_matches_user_job_version UNIQUE (user_id, job_id, resume_version)
);

-- 主查询路径：某用户在当前简历版本下的所有匹配，按分数排序
CREATE INDEX idx_matches_user_version_score ON job_matches (user_id, resume_version, score DESC);
-- 运维/重算用：找出所有走了降级的记录
CREATE INDEX idx_matches_engine ON job_matches (engine, computed_at) WHERE engine = 'RULE_BASED';
