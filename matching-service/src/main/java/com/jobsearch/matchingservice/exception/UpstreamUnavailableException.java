package com.jobsearch.matchingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 不可降级的上游挂了（目前只有 user-service）。
 *
 * <p>503 Service Unavailable —— 如实告诉客户端"是我们的问题，请稍后重试"，
 * 而不是返回 200 + 空结果假装正常。监控和前端都依赖这个状态码做判断。
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message) {
        super(message);
    }
}
