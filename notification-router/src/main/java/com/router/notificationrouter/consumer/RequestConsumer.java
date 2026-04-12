package com.router.notificationrouter.consumer;

import com.common.constants.KafkaTopics;
import com.common.event.NotificationEvent;
import com.router.notificationrouter.producer.RoutingProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestConsumer {

    private final RoutingProducer routingProducer;



    @KafkaListener(
            topics = KafkaTopics.NOTIFICATION_REQUEST,
            groupId = "router-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(NotificationEvent event, Acknowledgment ack) {
        MDC.put("correlationId", event.getCorrelationId());

        try {
            log.info("Routing notification: type={}, userId={}",
                    event.getType(), event.getUserId());

            routingProducer.route(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Routing failed: correlationId={}", event.getCorrelationId(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
