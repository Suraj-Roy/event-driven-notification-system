package com.router.notificationrouter.producer;

import com.common.constants.KafkaTopics;
import com.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoutingProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void route(NotificationEvent event) {
        String targetTopic = switch (event.getType().toUpperCase()) {
            case "EMAIL" -> KafkaTopics.NOTIFICATION_EMAIL;
            case "PUSH"  -> KafkaTopics.NOTIFICATION_PUSH;
            default -> throw new IllegalArgumentException(
                    "Unknown notification type: " + event.getType());
        };

        kafkaTemplate.send(targetTopic, event.getUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to route: topic={}, correlationId={}",
                                targetTopic, event.getCorrelationId());
                    } else {
                        log.info("Routed: topic={}, partition={}, correlationId={}",
                                targetTopic,
                                result.getRecordMetadata().partition(),
                                event.getCorrelationId());
                    }
                });
    }
}