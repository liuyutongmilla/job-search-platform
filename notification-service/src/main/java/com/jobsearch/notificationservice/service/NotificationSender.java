package com.jobsearch.notificationservice.service;

import com.jobsearch.notificationservice.config.NotificationProperties;
import com.jobsearch.notificationservice.event.MatchComputedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 通知发送。
 *
 * <p>目前只打日志（和 banking 项目的 {@code MessageFunctions} 一样是桩实现）。
 * 接真实的邮件/短信网关时，把这里换成对应的 client，接口不变。
 */
@Service
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final NotificationProperties props;

    public NotificationSender(NotificationProperties props) {
        this.props = props;
    }

    public void sendEmail(MatchComputedEvent event) {
        if (!props.emailEnabled()) {
            log.debug("email channel disabled, skipping userId={}", event.userId());
            return;
        }
        // TODO 接真实邮件网关。注意：
        //  1) 网关调用失败要抛异常，让 Kafka 重试 / 进 DLQ，不能吞掉
        //  2) 邮件内容里【不要】放简历原文或手机号（个人敏感信息不出系统边界）
        //  3) 降级产生的匹配结果（engine=RULE_BASED）要在邮件里说明，
        //     否则用户会以为这些"快速匹配"结果是 AI 深度分析的
        log.info("[EMAIL] userId={} 有 {} 个新的职位匹配，最高分 {}（引擎：{}）",
                event.userId(), event.matchCount(), event.topScore(), event.engine());
    }

    public void sendSms(MatchComputedEvent event) {
        if (!props.smsEnabled()) {
            log.debug("sms channel disabled, skipping userId={}", event.userId());
            return;
        }
        log.info("[SMS] userId={} 有 {} 个新的职位匹配", event.userId(), event.matchCount());
    }
}
