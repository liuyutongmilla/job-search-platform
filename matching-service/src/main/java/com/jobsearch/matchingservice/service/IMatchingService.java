package com.jobsearch.matchingservice.service;

import com.jobsearch.matchingservice.dto.MatchResponseDto;

public interface IMatchingService {

    /** 触发一次匹配计算（会调 AI 或降级到规则打分），结果落库并发通知事件 */
    MatchResponseDto computeMatches(Long userId);

    /** 只读已算好的结果，不触发计算 */
    MatchResponseDto fetchMatches(Long userId);
}
