package com.jobsearch.userservice.entity;

/**
 * 简历解析状态机。
 *
 * <p>这个字段是异步链路的核心 —— 客户端上传后立刻拿到 202 + {@code PENDING}，
 * 之后轮询（或等 WebSocket 推送）直到变成 {@code DONE}。
 * 对应 banking 项目里 {@code communication_sw} 那个布尔位，
 * 只是这里用枚举，因为失败也是一种需要表达的状态。
 */
public enum ParseStatus {
    /** 已上传，等待 ai-service 消费 */
    PENDING,
    /** ai-service 正在处理 */
    PROCESSING,
    /** 解析完成，parsed_json 可用 */
    DONE,
    /** 解析失败，可人工重试 */
    FAILED
}
