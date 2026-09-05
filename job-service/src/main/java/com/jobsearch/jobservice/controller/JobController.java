package com.jobsearch.jobservice.controller;

import com.jobsearch.jobservice.constants.JobConstants;
import com.jobsearch.jobservice.dto.ErrorResponseDto;
import com.jobsearch.jobservice.dto.JobContactInfoDto;
import com.jobsearch.jobservice.dto.JobDto;
import com.jobsearch.jobservice.dto.ResponseDto;
import com.jobsearch.jobservice.service.IJobService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Jobs", description = "职位发布与检索")
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    private final IJobService jobService;
    private final JobContactInfoDto jobConfig;

    /** 来自 config server 的共享配置，用来验证配置中心是否真的生效了 */
    @Value("${build.version:unknown}")
    private String buildVersion;

    public JobController(IJobService jobService, JobContactInfoDto jobConfig) {
        this.jobService = jobService;
        this.jobConfig = jobConfig;
    }

    @Operation(summary = "发布职位")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败或职位已存在",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<JobDto> createJob(@Valid @RequestBody JobDto jobDto) {
        JobDto created = jobService.createJob(jobDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "按 ID 查职位")
    @GetMapping("/{jobId}")
    public ResponseEntity<JobDto> fetchJob(
            @RequestHeader(value = JobConstants.CORRELATION_ID, required = false) String correlationId,
            @PathVariable Long jobId) {
        logger.debug("fetchJob jobId={} correlationId={}", jobId, correlationId);
        return ResponseEntity.ok(jobService.fetchJob(jobId));
    }

    /**
     * 多条件检索 —— matching-service 通过 Feign 调的就是这个接口。
     *
     * <p>{@code @RateLimiter} 是服务自身的限流，和网关的 Redis 限流是两道独立防线：
     * 网关那道按用户限，这道按服务实例限，防止内部服务（比如批量重算）把数据库打满。
     */
    @Operation(summary = "多条件检索职位（所有条件可选）")
    @RateLimiter(name = "searchJobs", fallbackMethod = "searchFallback")
    @GetMapping("/search")
    public ResponseEntity<List<JobDto>> search(
            @RequestHeader(value = JobConstants.CORRELATION_ID, required = false) String correlationId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxYears,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(jobService.search(city, minSalary, maxYears, skill, limit));
    }

    /** 限流触发时的降级：返回空列表 + 429，而不是抛异常。 */
    public ResponseEntity<List<JobDto>> searchFallback(String correlationId, String city, Integer minSalary,
                                                       Integer maxYears, String skill, Integer limit,
                                                       Throwable throwable) {
        logger.warn("searchJobs rate-limited, returning empty result: {}", throwable.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(List.of());
    }

    @Operation(summary = "更新职位")
    @PutMapping("/{jobId}")
    public ResponseEntity<ResponseDto> updateJob(@PathVariable Long jobId,
                                                 @Valid @RequestBody JobDto jobDto) {
        jobService.updateJob(jobId, jobDto);
        return ResponseEntity.ok(new ResponseDto(JobConstants.STATUS_200, JobConstants.MESSAGE_200));
    }

    @Operation(summary = "关闭职位（软删除，置为 CLOSED）")
    @DeleteMapping("/{jobId}")
    public ResponseEntity<ResponseDto> closeJob(@PathVariable Long jobId) {
        jobService.closeJob(jobId);
        return ResponseEntity.ok(new ResponseDto(JobConstants.STATUS_200, JobConstants.MESSAGE_200));
    }

    @Operation(summary = "查看当前生效的配置（验证 config server 是否接通）")
    @GetMapping("/contact-info")
    public ResponseEntity<JobContactInfoDto> contactInfo() {
        return ResponseEntity.ok(jobConfig);
    }

    @Operation(summary = "查看构建版本（来自 config server 的共享 application.yml）")
    @GetMapping("/build-info")
    public ResponseEntity<String> buildInfo() {
        return ResponseEntity.ok(buildVersion);
    }
}
