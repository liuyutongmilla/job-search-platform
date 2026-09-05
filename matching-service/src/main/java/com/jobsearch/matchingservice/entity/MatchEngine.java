package com.jobsearch.matchingservice.entity;

/**
 * 这条匹配记录是谁算出来的。
 *
 * <p><b>把它落库是刻意的设计</b>：
 * <ul>
 *   <li>运维能查"昨天有多少比例的匹配走了降级"—— 降级率是核心 SLO 指标</li>
 *   <li>产品能对比两种引擎的用户点击率，回答"AI 到底值不值这个钱"</li>
 *   <li>AI 服务恢复后，可以定向重算那些 engine = RULE_BASED 的记录</li>
 * </ul>
 *
 * <p>如果不记这个字段，降级就变成了一件"发生了但没人知道"的事。
 */
public enum MatchEngine {
    /** Claude 打分 */
    AI,
    /** 规则打分（降级路径） */
    RULE_BASED
}
