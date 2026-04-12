package com.push.pushservice.consumer;

import com.common.constants.KafkaTopics;
import com.common.event.NotificationEvent;
import com.push.pushservice.service.IdempotencyService;
import com.push.pushservice.service.PushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushConsumer {

    private final IdempotencyService idempotencyService;
    private final PushSender pushSender;


    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_PUSH,
            groupId = "push-service-group"
    )
    public void consume(NotificationEvent event, Acknowledgment ack) {
        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("messageId", event.getMessageId());

        try {

            if (idempotencyService.isProcessed(event.getMessageId())) {
                log.info("Duplicate skipped: messageId={}", event.getMessageId());
                ack.acknowledge();
                return;
            }

            pushSender.send(event.getUserId(), event.getSubject(), event.getBody());
            idempotencyService.markProcessed(event.getMessageId());
            ack.acknowledge();

            log.info("Push sent: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("Push failed: messageId={}", event.getMessageId(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
