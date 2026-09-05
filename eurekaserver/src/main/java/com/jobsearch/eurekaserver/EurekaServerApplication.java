package com.jobsearch.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 服务注册中心。
 *
 * <p>这是 banking 项目里没有的模块 —— 那个项目把服务发现交给了 Kubernetes
 * （spring-cloud-starter-kubernetes-discoveryclient + K8s Service DNS）。
 * 换成 Eureka 之后有两处连带改动，别漏：
 * <ul>
 *   <li>Feign 客户端去掉 {@code url}：{@code @FeignClient(name = "job-service")}，
 *       由 Spring Cloud LoadBalancer 做客户端负载均衡</li>
 *   <li>网关路由从 {@code .uri("http://job-service:8090")} 改成 {@code .uri("lb://JOB-SERVICE")}</li>
 * </ul>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
