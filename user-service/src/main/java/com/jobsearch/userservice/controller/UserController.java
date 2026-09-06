package com.jobsearch.userservice.controller;

import com.jobsearch.userservice.dto.ErrorResponseDto;
import com.jobsearch.userservice.dto.ResponseDto;
import com.jobsearch.userservice.dto.ResumeDto;
import com.jobsearch.userservice.dto.UserDto;
import com.jobsearch.userservice.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Users", description = "用户资料与简历")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String CORRELATION_ID = "jobsearch-correlation-id";

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "注册用户")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "邮箱已注册或参数不合法",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @Operation(summary = "查用户资料")
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> fetchUser(
            @RequestHeader(value = CORRELATION_ID, required = false) String correlationId,
            @PathVariable Long userId) {
        logger.debug("fetchUser userId={} correlationId={}", userId, correlationId);
        return ResponseEntity.ok(userService.fetchUser(userId));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/{userId}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable Long userId,
                                                  @Valid @RequestBody UserDto userDto) {
        userService.updateUser(userId, userDto);
        return ResponseEntity.ok(new ResponseDto("200", "Request processed successfully"));
    }

    
    @Operation(summary = "上传简历（异步解析，立即返回 202）")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "已接受，后台解析中"),
            @ApiResponse(responseCode = "400", description = "文件为空或格式不支持",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping(path = "/{userId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeDto> uploadResume(@PathVariable Long userId,
                                                  @RequestPart("file") MultipartFile file) {
        ResumeDto dto = userService.uploadResume(userId, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    @Operation(summary = "查最新简历及解析状态（matching-service 通过 Feign 调这个）")
    @GetMapping("/{userId}/resume")
    public ResponseEntity<ResumeDto> fetchResume(
            @RequestHeader(value = CORRELATION_ID, required = false) String correlationId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(userService.fetchLatestResume(userId));
    }
}
