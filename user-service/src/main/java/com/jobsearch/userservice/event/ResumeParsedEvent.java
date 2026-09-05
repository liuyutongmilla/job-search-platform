package com.jobsearch.userservice.event;

/**
 * ai-service ──► topic {@code resume-parsed} ──► user-service（回写）
 *
 * <p>这是"最终一致性"的回程。上传接口早就返回 202 了，
 * 这条消息到达时才把 parse_status 置为 DONE。
 *
 * <p>{@code success = false} 时 {@code parsedJson} 为空、{@code errorMessage} 有值 ——
 * 失败也必须是一条正常投递的消息，而不是"没有消息"。
 * 否则下游永远等不到结果，也不知道该等多久。
 */
public record ResumeParsedEvent(
        Long resumeId,
        Long userId,
        Integer version,
        boolean success,
        String parsedJson,
        String errorMessage
) {
}
