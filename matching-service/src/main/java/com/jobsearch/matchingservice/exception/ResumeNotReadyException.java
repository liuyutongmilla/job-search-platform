package com.jobsearch.matchingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 简历还没解析完，无法匹配。
 *
 * <p>用 409 Conflict 而不是 400：请求本身是合法的，
 * 只是<b>当前资源状态</b>不允许这个操作。语义上 409 更准，
 * 而且客户端可以据此决定"稍后重试"，而 400 意味着"改请求再来"。
 * 状态码选对了，客户端的重试逻辑才写得对。
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ResumeNotReadyException extends RuntimeException {

    public ResumeNotReadyException(String message) {
        super(message);
    }
}
