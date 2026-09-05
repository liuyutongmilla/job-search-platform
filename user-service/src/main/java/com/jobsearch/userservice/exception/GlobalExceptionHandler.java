package com.jobsearch.userservice.exception;

import com.jobsearch.userservice.dto.ErrorResponseDto;
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
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(
            UserAlreadyExistsException ex, WebRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResumeStorageException.class)
    public ResponseEntity<ErrorResponseDto> handleStorage(
            ResumeStorageException ex, WebRequest request) {
        return build(request, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 文件超过 spring.servlet.multipart.max-file-size 时 Spring 抛这个 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleTooLarge(
            MaxUploadSizeExceededException ex, WebRequest request) {
        return build(request, HttpStatus.PAYLOAD_TOO_LARGE, "简历文件过大，请压缩后重试");
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
