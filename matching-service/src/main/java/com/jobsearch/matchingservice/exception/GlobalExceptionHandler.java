package com.jobsearch.matchingservice.exception;

import com.jobsearch.matchingservice.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResumeNotReadyException.class)
    public ResponseEntity<ErrorResponseDto> handleResumeNotReady(
            ResumeNotReadyException ex, WebRequest request) {
        
        log.info("resume not ready: {}", ex.getMessage());
        return build(request, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UpstreamUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleUpstreamUnavailable(
            UpstreamUnavailableException ex, WebRequest request) {
        
        log.warn("upstream unavailable: {}", ex.getMessage());
        return build(request, HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

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
