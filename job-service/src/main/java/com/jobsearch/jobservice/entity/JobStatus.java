package com.jobsearch.jobservice.entity;

public enum JobStatus {
    /** 在招 */
    OPEN,
    /** 已关闭（下架 / 招满）—— 不物理删除，保留历史用于审计和匹配记录追溯 */
    CLOSED
}
