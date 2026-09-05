package com.jobsearch.jobservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 统一错误响应体。所有异常最终都收敛成这个形状 —— 这就是《接口报文规范》里
 * "统一错误码"那一节在代码里的落点。
 */
@Schema(name = "ErrorResponse", description = "统一错误响应")
public record ErrorResponseDto(
        String apiPath,
        HttpStatus errorCode,
        String errorMessage,
        LocalDateTime errorTime
) {
}
