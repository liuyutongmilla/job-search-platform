-- 每个微服务一个独立数据库：服务之间不共享表，只通过 API / Kafka 交互。
-- 这是微服务的硬约束 —— 一旦两个服务直连同一张表，你就失去了独立部署的能力。

CREATE DATABASE userdb     OWNER jobsearch;
CREATE DATABASE jobdb      OWNER jobsearch;
CREATE DATABASE matchingdb OWNER jobsearch;
