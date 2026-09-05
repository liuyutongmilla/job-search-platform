package com.jobsearch.jobservice.exception;

import com.jobsearch.jobservice.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常出口 —— 所有异常在这里收敛成统一的 {@link ErrorResponseDto}。
 *
 * <p>直接从 banking 项目搬过来，只改了两处：
 * <ol>
 *   <li>{@code @ControllerAdvice} → {@code @RestControllerAdvice}（省掉每个方法的 {@code @ResponseBody}）</li>
 *   <li>兜底的 {@code Exception} 处理器加了 {@code log.error} —— banking 那版把异常
 *       堆栈直接吞了只返回 message，生产上排障会很痛苦</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 参数校验失败 → 400 + 字段级错误明细，前端可以直接把错误标到对应输入框上 */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            validationErrors.put(field, error.getDefaultMessage());
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        return build(request, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(JobAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleJobAlreadyExists(
            JobAlreadyExistsException ex, WebRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 兜底。注意这里 {@code log.error} 带上了异常对象 —— 堆栈必须进日志，
     * 但<b>不能</b>返回给客户端（会泄漏内部结构）。所以响应里给的是一句通用提示。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobal(Exception ex, WebRequest request) {
        log.error("unhandled exception on {}", request.getDescription(false), ex);
        return build(request, HttpStatus.INTERNAL_SERVER_ERROR, "服务内部错误，请联系管理员并提供 correlation-id");
    }

    private ResponseEntity<ErrorResponseDto> build(WebRequest request, HttpStatus status, String message) {
        return new ResponseEntity<>(
                new ErrorResponseDto(request.getDescription(false), status, message, LocalDateTime.now()),
                status);
    }
}
