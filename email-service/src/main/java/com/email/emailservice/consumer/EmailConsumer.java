package com.email.emailservice.consumer;

import com.common.constants.KafkaTopics;
import com.common.event.NotificationEvent;
import com.email.emailservice.service.EmailSender;
import com.email.emailservice.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {

    private final IdempotencyService idempotencyService;
    private final EmailSender emailSender;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EMAIL, groupId = "email-service-group")
    public void consume(NotificationEvent event, Acknowledgment ack) {
        // MDC.put("correlationId", event.getCorrelationId());
        // MDC.put("messageId", event.getMessageId());
        log.info("Email request received: messageId={}", event.getMessageId());

        try {
            // Step 1: Idempotency check
            if (idempotencyService.isProcessed(event.getMessageId())) {
                log.info("Duplicate skipped: messageId={}", event.getMessageId());
                ack.acknowledge();
                return;
            }

            // Step 2: Send email
            emailSender.send(event.getRecipient(), event.getSubject(), event.getBody());

            // Step 3: Mark processed
            idempotencyService.markProcessed(event.getMessageId());

            // Step 4: Acknowledge
            ack.acknowledge();

            log.info("Email sent: to={}, userId={}", event.getRecipient(), event.getUserId());

        } catch (Exception e) {
            log.error("Email failed: messageId={}", event.getMessageId(), e);
            throw e;
        } 
    }
}
