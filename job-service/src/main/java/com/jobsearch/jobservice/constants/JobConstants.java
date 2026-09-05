package com.jobsearch.jobservice.constants;

public final class JobConstants {

    private JobConstants() {
    }

    /** 网关注入的业务流水号，和 gatewayserver 的 FilterUtility.CORRELATION_ID 必须一致 */
    public static final String CORRELATION_ID = "jobsearch-correlation-id";

    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "Request processed successfully";
    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "Job created successfully";
}
