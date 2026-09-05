package com.jobsearch.jobservice.event;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 发到 Kafka topic {@code job-posted} 的事件。
 *
 * <p>事件载荷设计原则：<b>只带下游判断"要不要处理"所需的最小信息</b>，
 * 不要把整个实体塞进去。下游需要详情时用 jobId 回查 —— 这样 job-service
 * 改字段不会破坏所有消费者。
 */
public record JobPostedEvent(
        Long jobId,
        String title,
        String city,
        Set<String> skills,
        LocalDateTime postedAt
) {
}
