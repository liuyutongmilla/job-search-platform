package com.jobsearch.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 集中配置中心。
 *
 * <p>作用：把"每个环境不一样的参数"从代码里搬出去，改参数不需要重新构建发版
 * —— 这是你在银行做投产时最痛的那一环的解法。
 *
 * <p>本项目用 {@code native} profile，读 {@code classpath:/config/} 下的文件，
 * 好处是离线可跑、方便学习。生产环境应改成 git backend，把配置放独立仓库，
 * 这样改配置有 commit 记录、可 review、可回滚。
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
