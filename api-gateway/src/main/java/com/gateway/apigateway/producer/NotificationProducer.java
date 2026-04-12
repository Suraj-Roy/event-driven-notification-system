package com.gateway.apigateway.producer;

import com.common.constants.KafkaTopics;
import com.common.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;



    public void send(NotificationEvent event) {
        // WHY key = userId: All notifications for the same user go to
        // the same partition → processed in order.
        kafkaTemplate.send(KafkaTopics.NOTIFICATION_REQUEST,
                        event.getUserId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to produce: correlationId={}, error={}",
                                event.getCorrelationId(), ex.getMessage());
                    } else {
                        log.info("Produced: topic={}, partition={}, offset={}, correlationId={}",
                                KafkaTopics.NOTIFICATION_REQUEST,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                event.getCorrelationId());
                    }
                });
    }
}

