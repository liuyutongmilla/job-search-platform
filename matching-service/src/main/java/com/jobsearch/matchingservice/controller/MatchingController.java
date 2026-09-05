package com.jobsearch.matchingservice.controller;

import com.jobsearch.matchingservice.dto.ErrorResponseDto;
import com.jobsearch.matchingservice.dto.MatchResponseDto;
import com.jobsearch.matchingservice.service.IMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Matching", description = "简历-职位匹配")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class MatchingController {

    private final IMatchingService matchingService;

    public MatchingController(IMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    /**
     * 触发一次匹配计算。
     *
     * <p>用 POST 而不是 GET：这个操作有副作用（花钱调 AI、写数据库、发消息），
     * 不满足 GET 的幂等和安全语义。写成 GET 的后果是浏览器、代理、
     * 以及网关上配的 GET 重试策略会重复触发它 —— 那可是真金白银。
     */
    @Operation(summary = "触发匹配计算（有副作用，会调用 AI）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "计算完成（注意检查 degraded 字段）"),
            @ApiResponse(responseCode = "409", description = "简历还没解析完",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "503", description = "关键上游不可用",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/compute")
    public ResponseEntity<MatchResponseDto> compute(@RequestParam Long userId) {
        return ResponseEntity.ok(matchingService.computeMatches(userId));
    }

    @Operation(summary = "读取已算好的匹配结果（无副作用）")
    @GetMapping("/matches")
    public ResponseEntity<MatchResponseDto> fetch(@RequestParam Long userId) {
        return ResponseEntity.ok(matchingService.fetchMatches(userId));
    }
}
