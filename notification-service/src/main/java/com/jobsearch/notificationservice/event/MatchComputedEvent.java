package com.jobsearch.notificationservice.event;

/**
 * matching-service 发出的事件，在这里重新声明一份本地视图。
 *
 * <p>不从 matching-service 依赖 jar 过来 —— 理由和 {@code ResumeView} 一样：
 * 避免编译期耦合。Jackson 会忽略未知字段，所以上游加字段不影响这里。
 */
public record MatchComputedEvent(
        Long userId,
        Integer resumeVersion,
        int matchCount,
        Integer topScore,
        String engine
) {
}
