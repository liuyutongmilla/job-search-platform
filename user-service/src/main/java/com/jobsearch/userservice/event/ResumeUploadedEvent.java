package com.jobsearch.userservice.event;

/**
 * user-service ──► topic {@code resume-uploaded} ──► ai-service
 *
 * <p>注意载荷里只有 {@code fileKey}，不带文件内容 —— 几 MB 的 PDF 塞进 Kafka 消息
 * 会撑爆消息大小限制、拖垮 broker。文件走对象存储，消息只传引用。
 * 这是消息设计的基本纪律。
 */
public record ResumeUploadedEvent(
        Long resumeId,
        Long userId,
        String fileKey,
        String contentType,
        Integer version
) {
}
