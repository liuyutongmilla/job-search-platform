package com.jobsearch.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Response", description = "统一成功响应")
public record ResponseDto(String statusCode, String statusMsg) {
}
